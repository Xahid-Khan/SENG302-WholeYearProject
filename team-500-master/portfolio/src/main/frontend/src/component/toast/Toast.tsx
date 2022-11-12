import React from "react";
import styles from "./Toast.module.css"
import defaultStyles from "./DefaultToast.module.css"
import {ToastTheme} from "./ToastTheme";
import {getClassNameFromThemes} from "./internal/util";
import {ToastBase, ToastBaseProps} from "./ToastBase";

interface ToastProps extends Omit<ToastBaseProps, "themes"> {
    title: string
    subtitle?: string

    before?: React.ReactNode
    after?: React.ReactNode
    theme?: ToastTheme
}

/**
 * Component that represents a basic Toast with a title and optionally a subtitle. Before and After components and a
 * custom theme can be passed to allow for customisation. This component also inherits the basic functionality provided
 * by BaseToast (i.e. dismissable and onDismiss).
 *
 * @param title of the toast. This is shown as the primary text of the toast.
 * @param subtitle of the toast. This is displayed as secondary text underneath the title of the toast.
 * @param dismissable specifies whether the user can dismiss the toast.
 * @param onDismiss called when the user dismisses the toast (if toast is dismissable)
 * @param theme to apply to the toast.
 *        This is usually an imported CSS module, but any object that maps keys to className strings works.
 *        By default, DefaultToast.module.css is used.
 * @param before component to show horizontally before the central title/subtitle block
 * @param after component to show horizontally after the central title/subtitle block
 */
export const Toast: React.FC<ToastProps> = ({title, subtitle, dismissable, onDismiss, theme = defaultStyles, before, after}) => {
    return (
        <ToastBase themes={[styles, theme]} dismissable={dismissable} onDismiss={onDismiss}>
            {(before) ? (
                <div className={getClassNameFromThemes("before", theme)}>{before}</div>
            ) : undefined}

            <div className={getClassNameFromThemes("body", theme)}>
                <div className={getClassNameFromThemes("title", theme)}>{title}</div>
                {(subtitle) ? (
                    <div className={getClassNameFromThemes("subtitle", theme)}>{subtitle}</div>
                ) : undefined}
            </div>

            {(after) ? (
                <div className={getClassNameFromThemes("after", theme)}>{after}</div>
            ) : undefined}
        </ToastBase>
    )
}