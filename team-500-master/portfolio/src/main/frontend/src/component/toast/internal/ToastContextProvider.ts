import {createContext, useContext} from "react";
import {ToasterStore} from "./ToasterStore";
import {ToastContext} from "../ToastContext";

const ToastContextContext = createContext<ToastContext | null>(null)

export const ToastContextProvider = ToastContextContext.Provider

export const useToast = () => {
    const store = useContext(ToastContextContext)
    if (store === null) {
        throw new Error("useToast must be used in a toasted component (one that is being presented by a Toaster)")
    }
    return store
}