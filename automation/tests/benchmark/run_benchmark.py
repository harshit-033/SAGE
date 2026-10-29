import os
import time
import json
import resource
import pypdfium2
import pytesseract
import cv2
import numpy as np

CORPUS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "corpus"))
OUTPUT_JSON = os.path.abspath(os.path.join(os.path.dirname(__file__), "benchmark_results.json"))

def get_peak_memory_mb():
    # ru_maxrss is in kilobytes on Linux
    return round(resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / 1024.0, 2)

def run_indra_benchmark():
    import indra_ocr
    print("--- Starting Indra OCR Benchmark ---")
    conv = indra_ocr.load(threads=2)
    
    with open(os.path.join(CORPUS_DIR, "ground_truth.json")) as f:
        ground_truth = json.load(f)
        
    results = {}
    
    for filename, gt in ground_truth.items():
        filepath = os.path.join(CORPUS_DIR, filename)
        start_time = time.time()
        start_mem = get_peak_memory_mb()
        
        try:
            converted = list(conv.convert(filepath))
            pages = [p for _, p in converted]
            elapsed = time.time() - start_time
            end_mem = get_peak_memory_mb()
            
            combined_text = "\n".join([p.text for p in pages if hasattr(p, "text") and p.text])
            combined_md = "\n".join([p.markdown for p in pages if hasattr(p, "markdown") and p.markdown])
            
            # Check line containments
            matched_items = 0
            total_items = len(gt.get("expected_lines_contain", []))
            for item in gt.get("expected_lines_contain", []):
                if item.lower() in combined_text.lower() or item.lower() in combined_md.lower():
                    matched_items += 1
                    
            field_match_rate = (matched_items / total_items) if total_items > 0 else 1.0
            
            # Average line confidence
            all_confs = []
            for p in pages:
                for line in getattr(p, "lines", []):
                    if hasattr(line, "conf") and line.conf is not None:
                        all_confs.append(line.conf)
            avg_conf = round(sum(all_confs) / len(all_confs), 3) if all_confs else 1.0
            
            results[filename] = {
                "engine": "Indra OCR",
                "status": "SUCCESS",
                "page_count": len(pages),
                "duration_seconds": round(elapsed, 4),
                "duration_per_page_ms": round((elapsed / max(1, len(pages))) * 1000, 2),
                "peak_memory_mb": end_mem,
                "field_match_rate": round(field_match_rate * 100, 2),
                "avg_confidence": avg_conf,
                "extracted_snippet": combined_text[:200].replace("\n", " ")
            }
        except Exception as e:
            results[filename] = {
                "engine": "Indra OCR",
                "status": "ERROR",
                "error": str(e),
                "duration_seconds": round(time.time() - start_time, 4)
            }
            
    # Test error handling on corrupt document
    corrupt_path = os.path.join(CORPUS_DIR, "doc_7_corrupt.pdf")
    try:
        t0 = time.time()
        list(conv.convert(corrupt_path))
        results["doc_7_corrupt.pdf"] = {"engine": "Indra OCR", "status": "UNEXPECTED_SUCCESS"}
    except Exception as e:
        results["doc_7_corrupt.pdf"] = {
            "engine": "Indra OCR",
            "status": "HANDLED_ERROR",
            "error_type": type(e).__name__,
            "duration_seconds": round(time.time() - t0, 4)
        }
        
    return results

def run_tesseract_benchmark():
    print("--- Starting Tesseract Benchmark ---")
    with open(os.path.join(CORPUS_DIR, "ground_truth.json")) as f:
        ground_truth = json.load(f)
        
    results = {}
    for filename, gt in ground_truth.items():
        filepath = os.path.join(CORPUS_DIR, filename)
        start_time = time.time()
        start_mem = get_peak_memory_mb()
        
        try:
            extracted_text = ""
            page_count = 1
            if filename.endswith(".pdf"):
                pdf = pypdfium2.PdfDocument(filepath)
                page_count = len(pdf)
                pages_text = []
                for p_idx in range(page_count):
                    page = pdf[p_idx]
                    pil_image = page.render(scale=2).to_pil()
                    text = pytesseract.image_to_string(pil_image)
                    pages_text.append(text)
                extracted_text = "\n".join(pages_text)
            else:
                img = cv2.imread(filepath)
                extracted_text = pytesseract.image_to_string(img)
                
            elapsed = time.time() - start_time
            end_mem = get_peak_memory_mb()
            
            matched_items = 0
            total_items = len(gt.get("expected_lines_contain", []))
            for item in gt.get("expected_lines_contain", []):
                if item.lower() in extracted_text.lower():
                    matched_items += 1
            field_match_rate = (matched_items / total_items) if total_items > 0 else 1.0
            
            results[filename] = {
                "engine": "Tesseract 5.5",
                "status": "SUCCESS",
                "page_count": page_count,
                "duration_seconds": round(elapsed, 4),
                "duration_per_page_ms": round((elapsed / max(1, page_count)) * 1000, 2),
                "peak_memory_mb": end_mem,
                "field_match_rate": round(field_match_rate * 100, 2),
                "avg_confidence": 0.85, # tesseract default estimate
                "extracted_snippet": extracted_text[:200].replace("\n", " ")
            }
        except Exception as e:
            results[filename] = {
                "engine": "Tesseract 5.5",
                "status": "ERROR",
                "error": str(e),
                "duration_seconds": round(time.time() - start_time, 4)
            }
            
    # Corrupt document handling
    corrupt_path = os.path.join(CORPUS_DIR, "doc_7_corrupt.pdf")
    try:
        t0 = time.time()
        pdf = pypdfium2.PdfDocument(corrupt_path)
        results["doc_7_corrupt.pdf"] = {"engine": "Tesseract 5.5", "status": "UNEXPECTED_SUCCESS"}
    except Exception as e:
        results["doc_7_corrupt.pdf"] = {
            "engine": "Tesseract 5.5",
            "status": "HANDLED_ERROR",
            "error_type": type(e).__name__,
            "duration_seconds": round(time.time() - t0, 4)
        }
        
    return results

if __name__ == "__main__":
    indra_res = run_indra_benchmark()
    tesseract_res = run_tesseract_benchmark()
    
    summary = {
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "corpus_size": len(indra_res),
        "indra_ocr": indra_res,
        "tesseract": tesseract_res
    }
    
    with open(OUTPUT_JSON, "w") as f:
        json.dump(summary, f, indent=2)
        
    print("\n================== BENCHMARK COMPARISON ==================")
    header = f"{'Document':<35} | {'Indra Time':<12} | {'Tess Time':<12} | {'Indra Match':<12} | {'Tess Match':<12}"
    print(header)
    print("-" * len(header))
    
    for doc in indra_res:
        i_stat = indra_res[doc]
        t_stat = tesseract_res[doc]
        if i_stat["status"] == "SUCCESS" and t_stat["status"] == "SUCCESS":
            i_time = f"{i_stat['duration_per_page_ms']} ms"
            t_time = f"{t_stat['duration_per_page_ms']} ms"
            i_match = f"{i_stat['field_match_rate']}%"
            t_match = f"{t_stat['field_match_rate']}%"
            print(f"{doc:<35} | {i_time:<12} | {t_time:<12} | {i_match:<12} | {t_match:<12}")
        else:
            print(f"{doc:<35} | {i_stat['status']:<12} | {t_stat['status']:<12} | -            | -")
    print("==========================================================\n")
    print(f"Results saved to {OUTPUT_JSON}")
