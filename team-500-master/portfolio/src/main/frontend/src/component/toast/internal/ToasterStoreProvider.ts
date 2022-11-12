import {createContext, useContext} from "react";
import {ToasterStore} from "./ToasterStore";

const ToasterStoreContext = createContext<ToasterStore | null>(null)

export const ToasterStoreProvider = ToasterStoreContext.Provider

export const useToasterStore = () => {
    const store = useContext(ToasterStoreContext)
    if (store === null) {
        throw new Error("useToaster must be used in a component that is wrapped in ToasterRoot.")
    }
    return store
}