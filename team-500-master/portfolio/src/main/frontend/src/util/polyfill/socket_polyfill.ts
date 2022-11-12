export const polyfill = () => {
    if ((window as any).global === undefined) {
        (window as any).global = window
    }
}

polyfill()