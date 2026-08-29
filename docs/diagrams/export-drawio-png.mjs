/**
 * draw.io → PNG 命令行导出（替代已失效的 draw.io-export 包）。
 *
 * 该包把渲染页写死在 https://www.draw.io/export3.html，域名已不可达且停止维护，
 * 会静默退出码 0 但不产文件。这里直连 app.diagrams.net 的同一张渲染页。
 *
 * 用法：
 *   node docs/diagrams/export-drawio-png.mjs moli-aiops-architecture
 *   node docs/diagrams/export-drawio-png.mjs --all
 *
 * 依赖 puppeteer。本机没装时先：
 *   npm i -D puppeteer
 * 或复用 npx 缓存：
 *   $env:NODE_PATH="<npx-cache>\node_modules"; node docs/diagrams/export-drawio-png.mjs --all
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const here = path.dirname(fileURLToPath(import.meta.url));
const pngDir = path.join(here, 'png');

const SCALE = 2;
const BORDER = 10;
const RENDER_PAGE = 'https://app.diagrams.net/export3.html';
const NAV_TIMEOUT_MS = Number(process.env.DRAWIO_EXPORT_TIMEOUT_MS || 240_000);
const NAV_ATTEMPTS = Number(process.env.DRAWIO_EXPORT_ATTEMPTS || 4);

function loadPuppeteer() {
  try {
    return require('puppeteer');
  } catch {
    console.error(
      '找不到 puppeteer。装一个：npm i -D puppeteer\n' +
        '或指向 npx 缓存：$env:NODE_PATH="<cache>\\node_modules"',
    );
    process.exit(1);
  }
}

function resolveTargets(args) {
  if (args.includes('--all')) {
    return fs
      .readdirSync(here)
      .filter((f) => f.endsWith('.drawio'))
      .map((f) => path.join(here, f));
  }
  const names = args.filter((a) => !a.startsWith('--'));
  if (names.length === 0) {
    console.error('用法：node export-drawio-png.mjs <图名，不带扩展名> | --all');
    process.exit(1);
  }
  return names.map((n) => {
    const p = n.endsWith('.drawio') ? n : `${n}.drawio`;
    return path.isAbsolute(p) ? p : path.join(here, p);
  });
}

/**
 * 打开 draw.io 的渲染页。
 *
 * 不等 networkidle0：这一页会拉 MathJax 等 CDN 资源，网络慢时永远等不到空闲。
 * 真正的就绪信号是 render() 被定义出来。外网到 app.diagrams.net 偶发
 * ERR_CONNECTION_CLOSED，所以带退避重试。
 */
async function openRenderPage(page) {
  let lastError;
  for (let attempt = 1; attempt <= NAV_ATTEMPTS; attempt += 1) {
    try {
      await page.goto(RENDER_PAGE, {
        waitUntil: 'domcontentloaded',
        timeout: NAV_TIMEOUT_MS,
      });
      await page.waitForFunction(() => typeof globalThis.render === 'function', {
        timeout: NAV_TIMEOUT_MS,
      });
      return;
    } catch (err) {
      lastError = err;
      console.error(`  渲染页打开失败（第 ${attempt}/${NAV_ATTEMPTS} 次）：${err.message}`);
      if (attempt < NAV_ATTEMPTS) {
        await new Promise((r) => setTimeout(r, 3000 * attempt));
      }
    }
  }
  throw lastError;
}

async function main() {
  const puppeteer = loadPuppeteer();
  const targets = resolveTargets(process.argv.slice(2));
  fs.mkdirSync(pngDir, { recursive: true });

  const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox'] });
  let failed = 0;
  try {
    const page = await browser.newPage();
    await openRenderPage(page);

    for (const src of targets) {
      const name = path.basename(src, '.drawio');
      const out = path.join(pngDir, `${name}.png`);
      try {
        const xml = fs.readFileSync(src, 'utf-8');

        // export3.html 暴露的 render() 会把结果画进页面，完成后插入 #LoadingComplete
        await page.evaluate(() => {
          const done = document.getElementById('LoadingComplete');
          if (done) done.remove();
        });
        await page.evaluate(
          (obj) => {
            // eslint-disable-next-line no-undef
            render(obj);
          },
          { xml, format: 'png', w: 0, h: 0, border: BORDER, bg: '#ffffff', scale: SCALE },
        );

        await page.waitForSelector('#LoadingComplete', { timeout: NAV_TIMEOUT_MS });
        const bounds = JSON.parse(
          await page.$eval('#LoadingComplete', (d) => d.getAttribute('bounds')),
        );

        await page.setViewport({
          width: Math.ceil(bounds.width),
          height: Math.ceil(bounds.height),
        });
        await page.screenshot({ type: 'png', fullPage: true, path: out });

        const kb = (fs.statSync(out).size / 1024).toFixed(0);
        console.log(`  ok  ${name}.png  (${Math.ceil(bounds.width)}x${Math.ceil(bounds.height)}, ${kb} KB)`);
      } catch (err) {
        failed += 1;
        console.error(`  FAIL ${name}: ${err.message}`);
      }
    }
  } finally {
    await browser.close();
  }

  if (failed > 0) process.exit(1);
}

await main();
