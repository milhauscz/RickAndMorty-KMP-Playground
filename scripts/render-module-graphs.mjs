#!/usr/bin/env node
/**
 * Render module-graph *.mmd files to PNG via @mermaid-js/mermaid-cli.
 */
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

const __dirname = dirname(fileURLToPath(import.meta.url));
const imagesDir = join(__dirname, "..", "docs", "images");
const background = "#f6f8fa";

const graphs = [
  { name: "module-graph-overview", width: 1000 },
  { name: "module-graph", width: 1400 },
  { name: "module-graph-core-common", width: 1200 },
  { name: "module-graph-core-network", width: 1200 },
  { name: "module-graph-core-database", width: 1200 },
  { name: "module-graph-core-designsystem", width: 1000 },
  { name: "module-graph-core-featureflags", width: 1000 },
  { name: "module-graph-core-image", width: 1000 },
];

for (const { name, width } of graphs) {
  const mmdFile = join(imagesDir, `${name}.mmd`);
  const pngFile = join(imagesDir, `${name}.png`);
  console.log(`Rendering ${name} ...`);
  const result = spawnSync(
    "npx",
    ["--yes", "@mermaid-js/mermaid-cli", "-i", mmdFile, "-o", pngFile, "-b", background, "-w", String(width)],
    { stdio: "inherit", shell: true },
  );
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

console.log("Done.");
