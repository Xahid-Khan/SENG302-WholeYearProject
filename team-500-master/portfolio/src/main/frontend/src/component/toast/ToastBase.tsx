import React, {useCallback} from "react";
import {useToasterStore} from "./internal/ToasterStoreProvider";
import {useToast} from "./internal/ToastContextProvider";
import {mergeClassNames} from "../../util/style_util";
import {getClassNameFromThemes} from "./internal/util";
import styles from "./ToastBase.module.css";
import {ToastTheme} from "./ToastTheme";

export interface ToastBaseProps {
    dismissable?: boolean
    onDismiss?: VoidFunction
    themes: ToastTheme[]
}
/**
 * Component that provides the basic structure and functionality of a Toast.
 *
 * @param dismissable specifies whether the user can dismiss the toast.
 * @param onDismiss called when the user dismisses the toast (if toast is dismissable).
 * @param themes to apply to the toast. This is usually an imported CSS module, but any object that maps keys to
 *        className strings works.
 * @param children component to place inside the toast.
 */
export const ToastBase: React.FC<ToastBaseProps> = ({dismissable = true, onDismiss, themes, children}) => {
    const toaster = useToasterStore()
    const toast = useToast()

    const dismissToast = useCallback(() => {
        toaster.dismiss(toast.id)
        if (onDismiss !== undefined) {
            onDismiss()
        }
    }, [toaster, toast, onDismiss])

    return (
        <div
            className={mergeClassNames(getClassNameFromThemes("toast", ...themes), (dismissable) ? styles.dismissable : undefined)}
            onClick={(dismissable) ? dismissToast : undefined}
        >
            {children}
        </div>
    )
}