import {ReactNode} from "react";
import {action, makeObservable, observable} from "mobx";

interface ToastData {
    component: () => ReactNode,
    timeout: number,
    created: Date,
    id: number
}

/**
 * Properties that can be used to configure a toast.
 */
interface ToasterStoreShowProps {
    /**
     * The number of milliseconds after which the toast will be automatically dismissed.
     */
    timeout: number
}
const DEFAULT_TOASTER_STORE_SHOW_PROPS = {
    timeout: 1000
}

/**
 * Stores and manages active 'toasts' (notifications / status updates for the user).
 */
export class ToasterStore {
    toasts: ToastData[]
    toastsById: Map<number, ToastData>

    protected nextId: number = 0
    protected generateId() {
        return this.nextId++
    }

    constructor() {
        makeObservable(this, {
            toasts: observable,
            toastsById: observable,

            show: action,
            dismiss: action
        })

        this.toasts = observable.array()
        this.toastsById = observable.map()
    }

    /**
     * Dismiss a toast as a result of a timeout callback expiring.
     *
     * This function double-checks that the timeout for the toast has actually expired, since a `replace` in the meantime
     * may have caused the timeout to be reset.
     */
    protected timeoutDismissToast(toastId: number) {
        const toast = this.toastsById.get(toastId)
        if (toast !== undefined && new Date() >= new Date(toast.created.getTime() + toast.timeout)) {
            this.dismiss(toastId)
        }
    }

    /**
     * Dismisses a toast and removes it from display. This will not cause onDismiss callbacks on Toasts to be called.
     *
     * @param id of the toast to dismiss. If the toast does not exist then this call will safely succeed.
     */
    public dismiss(id: number): void {
        this.toastsById.delete(id)
        const toastIndex = this.toasts.findIndex(toast => toast.id === id)
        if (toastIndex !== -1) {
            this.toasts.splice(toastIndex, 1)
        }
    }

    /**
     * Replaces an existing toast with a new toast and updated props. Note that this will reset and replace the timeout.
     *
     * @param id of the toast to replace
     * @param component to display
     * @param props for the toast. See {@link ToasterStoreShowProps}
     * @throws Error if a toast with the given id does not exist.
     */
    public replace(id: number, component: () => ReactNode, props: ToasterStoreShowProps = DEFAULT_TOASTER_STORE_SHOW_PROPS): void {
        const toastIndex = this.toasts.findIndex(toast => toast.id === id)
        if (toastIndex === -1 || !this.toastsById.has(id)) {
            throw new Error("Cannot replace a toast that does not exist.")
        }

        const newToast: ToastData = {
            id: id,
            component,
            timeout: props.timeout,
            created: new Date()
        }

        this.toasts.splice(toastIndex, 1, newToast)
        this.toastsById.set(id, newToast)

        if (props.timeout !== Infinity) {
            setTimeout(() => this.timeoutDismissToast(id), props.timeout)
        }
    }

    /**
     * Adds a new toast, returning an ID that can be used to perform operations on that toast in future.
     *
     * @param component to display
     * @param props see {@link ToasterStoreShowProps}
     */
    public show(component: () => ReactNode, props: ToasterStoreShowProps = DEFAULT_TOASTER_STORE_SHOW_PROPS): number {
        const toast = {
            id: this.generateId(),
            component,
            timeout: props.timeout,
            created: new Date()
        }

        this.toasts.push(toast)
        this.toastsById.set(toast.id, toast)

        if (props.timeout !== Infinity) {
            setTimeout(() => this.timeoutDismissToast(toast.id), props.timeout)
        }

        return toast.id
    }
}