import React, {useState} from "react";
import {ToasterStoreProvider} from "./internal/ToasterStoreProvider";
import {ToasterStore} from "./internal/ToasterStore";
import {Toaster} from "./internal/Toaster";

/**
 * The root of the toast system. Provides the ToasterStore to all children and inserts the necessary components to
 * display toasts.
 */
export const ToasterRoot: React.FC = ({ children }) => {
    const [store, _] = useState(() => new ToasterStore())

    return (
        <ToasterStoreProvider value={store}>
            <Toaster/>
            {children}
        </ToasterStoreProvider>
    )
}