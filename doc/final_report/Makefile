all: final_report.pdf

final_report.pdf: *.tex bibliography.bib acm_proc_article-sp.cls
	rubber --pdf final_report

clean:
	rm -f final_report.pdf final_report.aux final_report.log final_report.blg final_report.bbl 