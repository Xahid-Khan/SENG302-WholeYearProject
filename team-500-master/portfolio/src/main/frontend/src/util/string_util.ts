/**
 * Pads the given number to be shown to the given number of places, adding 0's as left padding if necessary.
 *
 * @param number to stringify and pad as necessary.
 * @param places number of places to pad the number to.
 * @throws RangeError if the string representation of number is longer than places.
 */
export const leftPadNumber = (number: number | string, places: number) => {
    const numberString = `${number}`

    if (numberString.length >= places) {
        return numberString;
    }

    return ('0'.repeat(places - numberString.length)) + number;
}