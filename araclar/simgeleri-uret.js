'use strict';
/* Sitenin çizgi simgelerini (Eğitim Evi deposu: public/js/parcalar/02-ikonlar.js)
   Android vektör çizimlerine çevirir: app/src/main/res/drawable/ik_<ad>.xml.
   Böylece uygulamadaki simgeler sitedekilerle birebir aynıdır.

     node araclar/simgeleri-uret.js ../Egitim-Evi

   Simgeler 24x24, 1.8 kalınlığında yuvarlak uçlu çizgidir; renk kodda verilir
   (ImageView tint). circle, rect, line, polyline, polygon ve ellipse yola çevrilir. */

const fs = require('fs');
const path = require('path');

const siteKoku = process.argv[2];
if (!siteKoku) { console.error('Kullanım: node araclar/simgeleri-uret.js <Eğitim Evi deposu>'); process.exit(1); }
const kaynak = fs.readFileSync(path.join(siteKoku, 'public', 'js', 'parcalar', '02-ikonlar.js'), 'utf8');
const m = kaynak.match(/var IKONLAR = \{([\s\S]*?)\n\};/);
if (!m) { console.error('IKONLAR bulunamadı'); process.exit(1); }
// eslint-disable-next-line no-new-func
const IKONLAR = new Function('return {' + m[1] + '};')();
const cikti = path.join(__dirname, '..', 'app', 'src', 'main', 'res', 'drawable');

const sayi = v => Number(v);
function nitelikler(s) {
  const o = {};
  s.replace(/([a-z-]+)="([^"]*)"/g, (_, k, v) => { o[k] = v; });
  return o;
}
function yol(oge, a) {
  if (oge === 'path') return a.d;
  if (oge === 'circle' || oge === 'ellipse') {
    const cx = sayi(a.cx), cy = sayi(a.cy), rx = sayi(a.rx || a.r), ry = sayi(a.ry || a.r);
    return 'M' + (cx - rx) + ',' + cy + 'a' + rx + ',' + ry + ' 0 1,0 ' + (2 * rx) + ',0a' + rx + ',' + ry + ' 0 1,0 ' + (-2 * rx) + ',0';
  }
  if (oge === 'rect') {
    const x = sayi(a.x), y = sayi(a.y), w = sayi(a.width), h = sayi(a.height), r = sayi(a.rx || 0);
    if (!r) return 'M' + x + ',' + y + 'h' + w + 'v' + h + 'h' + (-w) + 'z';
    return 'M' + (x + r) + ',' + y + 'h' + (w - 2 * r) + 'a' + r + ',' + r + ' 0 0,1 ' + r + ',' + r + 'v' + (h - 2 * r) +
      'a' + r + ',' + r + ' 0 0,1 ' + (-r) + ',' + r + 'h' + (-(w - 2 * r)) + 'a' + r + ',' + r + ' 0 0,1 ' + (-r) + ',' + (-r) +
      'v' + (-(h - 2 * r)) + 'a' + r + ',' + r + ' 0 0,1 ' + r + ',' + (-r) + 'z';
  }
  if (oge === 'line') return 'M' + a.x1 + ',' + a.y1 + 'L' + a.x2 + ',' + a.y2;
  if (oge === 'polyline' || oge === 'polygon') {
    const p = a.points.trim().split(/[\s,]+/);
    let d = 'M' + p[0] + ',' + p[1];
    for (let i = 2; i < p.length; i += 2) d += 'L' + p[i] + ',' + p[i + 1];
    return d + (oge === 'polygon' ? 'z' : '');
  }
  throw new Error('bilinmeyen öğe: ' + oge);
}

let adet = 0;
for (const [ad, svg] of Object.entries(IKONLAR)) {
  const parcalar = [];
  svg.replace(/<(path|circle|rect|line|polyline|polygon|ellipse)\b([^>]*)\/?>/g, (_, oge, n) => {
    const a = nitelikler(n);
    parcalar.push({ d: yol(oge, a), dolu: a.fill && a.fill !== 'none' });
  });
  if (!parcalar.length) continue;
  const xml = '<?xml version="1.0" encoding="utf-8"?>\n' +
    '<!-- Sitenin "' + ad + '" simgesi (02-ikonlar.js); araclar/simgeleri-uret.js üretir, elle değiştirme -->\n' +
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n' +
    '    xmlns:tools="http://schemas.android.com/tools" tools:ignore="UnusedResources"\n' +
    '    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">\n' +
    parcalar.map(p => '    <path android:pathData="' + p.d + '"\n        ' + (p.dolu
      ? 'android:fillColor="#FF000000"'
      : 'android:strokeColor="#FF000000" android:strokeWidth="1.8" android:strokeLineCap="round" android:strokeLineJoin="round"') +
      ' />').join('\n') + '\n</vector>\n';
  fs.writeFileSync(path.join(cikti, 'ik_' + ad.replace(/([A-Z])/g, '_$1').toLowerCase().replace(/[^a-z0-9_]/g, '_') + '.xml'), xml);
  adet++;
}
console.log(adet + ' simge yazıldı: ' + cikti);
