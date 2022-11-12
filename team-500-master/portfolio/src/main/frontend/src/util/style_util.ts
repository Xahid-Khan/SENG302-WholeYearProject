/**
 * Helper function that merges arbitrarily many className strings into a valid class string.
 *
 * @param classNames to merge. Falsey values are skipped.
 */
export const mergeClassNames = (...classNames: (string | null | undefined)[]): string => {
    return classNames.filter(name => !!name).join(" ")
}