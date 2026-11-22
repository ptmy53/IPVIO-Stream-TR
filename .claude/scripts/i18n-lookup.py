#!/usr/bin/env python3
# Reproduces the lookup of I18nCore.translate (exact rows, placeholder patterns, "A · B" and "Name: value"
# composites) for the English column, to check which German texts still show German in English.
# Read only. Not a test of the app: the device decides. Port written on 2026-11-22 (T-043).
#
# Usage: .claude/scripts/i18n-lookup.py "Alle Filme" "Fehler 403"
# Limits: English column only (index 1); no fallback rules for other languages; Kotlin regex and
# Python regex are assumed to behave the same for these patterns.
import re,glob,sys
import subprocess
REPO=subprocess.run(['git','rev-parse','--show-toplevel'],capture_output=True,text=True).stdout.strip()+'/'
PH=re.compile(r'\{(#?)(\d)\}')
DECO=re.compile(r'^([‹›\s]*)(.*?)([\s‹›]*)$', re.S)
DOT=re.compile(r'\s*·\s*')
exact={}; pats=[]
import os
exact={}; pats=[]
def _sources():
    base=REPO+'app/src/main/java/com/ipvio/stream/'
    srcs=[]
    if not os.environ.get('DICT_W_OFF'):
        # DictW main block only: the Polish block DictPlW is a separate string in the same file
        srcs.append(open(base+'DictW.kt',encoding='utf-8').read().split('"""')[1].splitlines())
    for f in sorted(glob.glob(base+'Dict[A-P].kt'))+sorted(glob.glob(base+'DictEn[0-9].kt')):
        srcs.append(open(f,encoding='utf-8').read().splitlines())
    return srcs
for lines in _sources():          # same order as I18nCore.load
    for l in lines:
        p=l.split('¦')
        if len(p)<2 or not p[0].strip(): continue
        k=p[0].strip(); en=p[1].strip() or k
        if PH.search(k):
            out=['^'];last=0;num=[]
            for m in PH.finditer(k):
                out.append(re.escape(k[last:m.start()])); out.append(r'([\d.,:]+)' if m.group(1)=='#' else '(.+?)'); num.append(m.group(1)=='#'); last=m.end()
            out.append(re.escape(k[last:])); out.append('$')
            pats.append((len(''.join(out)), re.compile(''.join(out),re.S), en, num))
        else:
            exact[k] = en   # later blocks win, as in I18nCore.fill
pats.sort(key=lambda x:-x[0])
def lookup(s, depth=0):
    if depth>4: return None
    if s in exact: return exact[s]
    d=DECO.match(s)
    if d:
        pre,core,suf=d.groups()
        if core and (pre or suf) and core!=s:
            r=lookup(core,depth+1)
            if r is not None: return pre+r+suf
    for _,rx,en,num in pats:
        m=rx.match(s)
        if not m: continue
        out=en
        for i,g in enumerate(m.groups()):
            v = g if (i<len(num) and num[i]) else (lookup(g,depth+1) or g)
            out=out.replace('{%d}'%i,v).replace('{#%d}'%i,v)
        return out
    seps=list(DOT.finditer(s))
    if seps:
        parts=[];last=0
        for m in seps: parts.append(s[last:m.start()]); last=m.end()
        parts.append(s[last:])
        tr=[]; anyhit=False
        for part in parts:
            r=lookup(part,depth+1)
            if r is not None: anyhit=True; tr.append(r)
            else: tr.append(part)
        if anyhit:
            out=tr[0]
            for i,m in enumerate(seps): out+=m.group(0)+tr[i+1]
            return out
    c=s.find(': ')
    if c>0:
        a=lookup(s[:c],depth+1); b=lookup(s[c+2:],depth+1)
        if a is not None or b is not None: return (a or s[:c])+': '+(b or s[c+2:])
    return None
def tr(s):
    r=lookup(s)
    return r if r is not None else s
if __name__=='__main__':
    for s in sys.argv[1:]:
        print(f"{s!r:65} -> {tr(s)!r}")
