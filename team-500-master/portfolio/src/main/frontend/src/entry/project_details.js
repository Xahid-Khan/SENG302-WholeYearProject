/**
 * Compiler entry to build utilities originally used in project_details JavaScript, but which has since been ported into
 * the TypeScript codebase.
 */
import {DatetimeUtils} from "../util/DatetimeUtils";
import {Socket} from "./live_updating";

window.DatetimeUtils = DatetimeUtils;
window.Socket = Socket;
