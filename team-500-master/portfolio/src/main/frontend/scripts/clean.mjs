import fs from "fs";
import {targetBuildDirectory} from "./shared.mjs";

if (fs.existsSync(targetBuildDirectory)) {
  fs.rmdirSync(targetBuildDirectory, {recursive: true})
}