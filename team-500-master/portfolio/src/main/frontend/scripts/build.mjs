/**
 * Build Script for the frontend app.
 *
 * Based on those found in https://github.com/tfinlay/tankcosc (MIT License)
 */
import fs from "fs";
import esbuild from "esbuild";
import cssModulesPlugin from "esbuild-css-modules-plugin";
import {entryPointPaths, targetBuildDirectory} from "./shared.mjs";

// Delete ../static/app/ if it exists
if (fs.existsSync(targetBuildDirectory)) {
    fs.rmSync(targetBuildDirectory, { recursive: true });
}

// Build Project
await esbuild.build({
    entryPoints: entryPointPaths,
    outdir: targetBuildDirectory,
    bundle: true,
    //minify: true,
    platform: "browser",
    sourcemap: "linked",
    plugins: [
        cssModulesPlugin()
    ],
    external: ['*.woff2', '*.woff']
});