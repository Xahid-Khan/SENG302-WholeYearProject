import React from "react";
import {observer} from "mobx-react-lite";
import {useToasterStore} from "./ToasterStoreProvider";
import styles from "./Toaster.module.css"
import { ToastContextProvider } from "./ToastContextProvider";

/**
 * Component that listens to updates in the ToasterStore and displays the toasts it contains.
 */
export const Toaster: React.FC = observer(() => {
    const store = useToasterStore()

    if (store.toasts.length === 0) {
        return <></>
    }
    else {
        return (
            <div className={styles.toaster}>
                {store.toasts.map(toast => (
                    // React.Fragment is a nothing component that just allows us to provide a key to the component
                    <ToastContextProvider key={toast.id} value={{
                        id: toast.id
                    }}>
                        {toast.component()}
                    </ToastContextProvider>
                ))}
            </div>
        )
    }
})