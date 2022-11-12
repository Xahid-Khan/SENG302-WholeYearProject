export const getAPIAbsolutePath = (globalUrlPathPrefix: string, relativePath: string) => {
    return `${location.protocol}//${location.host}${globalUrlPathPrefix}/api/v1/${relativePath}`
}

export const getAbsolutePath = (globalUrlPathPrefix: string, relativePath: string) => {
    return `${location.protocol}//${location.host}${globalUrlPathPrefix}/${relativePath}`
}