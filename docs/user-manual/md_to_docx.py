"""Converts user-manual.md to user-manual.docx (simple subset: headings, paragraphs, lists, tables, images, quotes).
Usage: python md_to_docx.py   (needs: pip install python-docx)"""
import os
import re

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.shared import Cm, Pt, RGBColor

HERE = os.path.dirname(os.path.abspath(__file__))
SRC, OUT = os.path.join(HERE, "user-manual.md"), os.path.join(HERE, "user-manual.docx")
PAGE_W = 16.0  # usable width in cm

doc = Document()
for s in doc.sections:
    s.left_margin = s.right_margin = Cm(2.5)
doc.styles["Normal"].font.name = "Calibri"
doc.styles["Normal"].font.size = Pt(11)


def runs(par, text):
    """Adds inline markdown (bold, italic, code, links) to a paragraph."""
    text = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", text)  # links -> text
    for tok in re.split(r"(\*\*[^*]+\*\*|`[^`]+`|\*[^*]+\*|_[^_]+_)", text):
        if not tok:
            continue
        if tok.startswith("**"):
            par.add_run(tok[2:-2]).bold = True
        elif tok.startswith("`"):
            r = par.add_run(tok[1:-1]); r.font.name = "Consolas"; r.font.size = Pt(10)
        elif (tok.startswith("*") or tok.startswith("_")) and len(tok) > 2:
            par.add_run(tok[1:-1]).italic = True
        else:
            par.add_run(tok)


def image(par, md, width):
    m = re.match(r"!\[([^\]]*)\]\(([^)]+)\)", md.strip())
    par.add_run().add_picture(os.path.join(HERE, m.group(2)), width=Cm(width))


lines = open(SRC, encoding="utf-8").read().splitlines()
i = 0
while i < len(lines):
    ln = lines[i]
    if not ln.strip() or ln.strip() == "---":
        i += 1; continue
    if ln.startswith("#"):
        lvl = len(ln) - len(ln.lstrip("#"))
        doc.add_heading(re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", ln[lvl:].strip()), level=min(lvl - 1, 3) if lvl > 1 else 0)
        i += 1; continue
    if ln.lstrip().startswith("|"):
        rows = []
        while i < len(lines) and lines[i].lstrip().startswith("|"):
            cells = [c.strip() for c in lines[i].strip().strip("|").split("|")]
            if not all(re.fullmatch(r":?-+:?", c) for c in cells if c):
                rows.append(cells)
            i += 1
        ncol = max(len(r) for r in rows)
        t = doc.add_table(rows=len(rows), cols=ncol)
        t.style = "Table Grid"; t.alignment = WD_TABLE_ALIGNMENT.CENTER
        has_img = any(c.startswith("![") for r in rows for c in r)
        for r_i, r in enumerate(rows):
            for c_i in range(ncol):
                cell = t.cell(r_i, c_i); par = cell.paragraphs[0]
                c = r[c_i] if c_i < len(r) else ""
                if c.startswith("!["):
                    image(par, c, PAGE_W / ncol - 0.4)
                else:
                    runs(par, c)
                    if r_i == 0 and not has_img:
                        for run in par.runs: run.bold = True
        doc.add_paragraph()
        continue
    if ln.startswith("!["):
        image(doc.add_paragraph(), ln, PAGE_W); i += 1; continue
    if ln.startswith(">"):
        text = []
        while i < len(lines) and lines[i].startswith(">"):
            text.append(lines[i].lstrip("> ").strip()); i += 1
        p = doc.add_paragraph(); p.paragraph_format.left_indent = Cm(0.6)
        runs(p, " ".join(text))
        for run in p.runs: run.font.color.rgb = RGBColor(0x1F, 0x4E, 0x79)
        continue
    m = re.match(r"^(\s*)(- \[ \] |- |\d+\. )(.*)", ln)
    if m:
        ordered = m.group(2)[0].isdigit()
        style = "List Number" if ordered else "List Bullet"
        if m.group(1): style += " 2"
        text = ("☐ " if "[ ]" in m.group(2) else "") + m.group(3)
        if ordered:
            p = doc.add_paragraph(); p.paragraph_format.left_indent = Cm(0.9 if m.group(1) else 0.6)
            p.paragraph_format.first_line_indent = Cm(-0.5); p.add_run(m.group(2).strip() + " "); runs(p, text); i += 1
            continue
        p = doc.add_paragraph(style=style); runs(p, text); i += 1
        continue
    if ln.startswith("   "):
        p = doc.add_paragraph(); p.paragraph_format.left_indent = Cm(0.6); runs(p, ln.strip()); i += 1; continue
    p = doc.add_paragraph(); runs(p, ln); i += 1

doc.save(OUT)
print("wrote", OUT)
