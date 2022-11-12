import styles from "../ToastBase.module.css";
import {ToastTheme} from "../ToastTheme";
import {mergeClassNames} from "../../../util/style_util";

export const getClassNameFromThemes = (name: keyof typeof styles, ...themes: ToastTheme[]) => {
    return mergeClassNames(styles[name], ...themes.map(theme => theme[name]))
}