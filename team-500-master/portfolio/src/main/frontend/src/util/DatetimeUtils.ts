import {leftPadNumber} from "./string_util";


/**
 * Utilities to make it easier to work with Dates.
 */
export class DatetimeUtils {
  /**
   * Convert a network datetime string (in UTC) to a JavaScript Date (in the local timezone).
   */
  static networkStringToLocalDate(utcString: string) {
    return new Date(Date.parse(utcString))
  }

  /**
   * Convert a JavaScript Date (in local timezone) to a network datetime string (in UTC)
   */
  static localToNetworkString(localDate: Date) {
    return localDate.toISOString();
  }

  /**
   * Convert a JavaScript Date (in local timezone) to a network datetime string (in local timezone)
   */
  static localToNetworkStringWithTimezone(localDate: Date) {
    let offsetDate = new Date(localDate)
    offsetDate.setHours(offsetDate.getHours() - (offsetDate.getTimezoneOffset() / 60)) // Offsets date to nullify offset caused by toISOString(), leaving date in local time
    return offsetDate.toISOString();
  }

  /**
   * Round a JavaScript Date (possibly including a time component) to the start of the day *in the local timezone*
   * and format it to a string of the format 'yyyy-mm-dd'
   */
  static toLocalYMD(localDate: Date) {
    return `${leftPadNumber(localDate.getFullYear(), 4)}-${leftPadNumber(localDate.getMonth() + 1, 2)}-${leftPadNumber(localDate.getDate(), 2)}`
  }

  /**
   * Parse a JavaScript Date (in the local timezone) from a string of the format 'yyyy-mm-dd'.
   *
   * Note that the string is assumed to be valid. No validation is done in this method.
   */
  static fromLocalYMD(localString: string) {
    // From: https://stackoverflow.com/a/64199706
    const [year, month, day] = localString.split('-');
    return new Date(parseInt(year, 10), parseInt(month, 10) - 1, parseInt(day, 10));
  }

  /**
   * Returns whethor or not that given JavaScript Date has a time component.
   */
  static hasTimeComponent(date: Date) {
    return date.getHours() !== 0 || date.getMinutes() !== 0 || date.getSeconds() !== 0
  }

  /**
   * Gets date and time and formats it to DMY and 12:00 PM format
   */
  static localToDMYWithTime(date: Date) {
    return `${date.getDate()} ${date.toLocaleString('default', {month: 'long'})} ${date.getFullYear()} ${date.getHours() == 0 || date.getHours() == 12 ? 12 : date.getHours() % 12}:${leftPadNumber(date.getMinutes(), 2)}${(date.getSeconds() !== 0) ? ':' + leftPadNumber(date.getSeconds(), 2) : ''} ${date.getHours() >= 12 ? 'PM' : 'AM'}`
  }

  /**
   * Converts the given Date into an HH:MM string in the local timezone.
   */
  static toLocalHM(date: Date) {
    return `${date.getHours()}:${leftPadNumber(date.getMinutes(), 2)}`
  }

  /**
   * Copies the given date, setting the hours and minutes component to match the hours and minutes parsed from the
   * hm string.
   *
   * @return Date with the new hours and minutes or null if hm is of an invalid format.
   */
  static withLocalHM(date: Date, hm: string): Date | null {
    const [hoursStr, minutesStr] = hm.split(/:/, 2)
    const hours = parseInt(hoursStr, 10)
    const minutes = parseInt(minutesStr, 10)
    if (isNaN(hours) || isNaN(minutes)) {
      return null
    }
    const newDate = new Date(date.getTime())
    newDate.setHours(hours)
    newDate.setMinutes(minutes)
    return newDate
  }

  /**
   * Format a JavaScript Date to a date string suitable for presentation to the user.
   */
  static localToUserDMY(localDate: Date) {
    return `${localDate.getDate()} ${localDate.toLocaleString('default', {month: 'long'})} ${localDate.getFullYear()}`;
  }

  /**
   * Format a JavaScript Date to a date string suitable for presentation to the user without time.
   */
  static localToUserDMYWithoutTime(localDate: Date) {
    return `${localDate.getDate()} ${localDate.toLocaleString('default', {month: 'long'})} ${localDate.getFullYear()}`;
  }

  /**
   * Checks whether the given dates are equal.
   */
  static areEqual(date1: Date, date2: Date) {
    return date1 <= date2 && date2 <= date1;
  }


  /**
   * Sets time to zero to check for all related events as it is only relevant for things that occur on the same day rather than the same time
   * Setting time to zero ensures that dates will be equal and not offset by time
   */
  static setTimeToZero(fullDate: Date) {
    return new Date(fullDate.getFullYear(), fullDate.getMonth(), fullDate.getDate()).getTime()
  }

  static timeStringToTimeSince(timeStamp: string) {

    const timestampDate = new Date(timeStamp);
    const currentDate = new Date();

    const secondsSince = (currentDate.getTime() - timestampDate.getTime()) / 1000;
    let timeSince;

    if (secondsSince < 60) {
      timeSince = "Now";
    } else if (secondsSince < 3600) {
      const timeFloor = Math.floor(secondsSince/60);
      timeSince = timeFloor + (timeFloor > 1 ? " minutes ago" : " minute ago");
    } else if (secondsSince < 86400) {
      const timeFloor = Math.floor(secondsSince/3600);
      timeSince = timeFloor + (timeFloor > 1 ? " hours ago" : " hour ago");
    } else {
      const timeFloor = Math.floor(secondsSince/86400);
      timeSince = timeFloor + (timeFloor > 1 ? " days ago" : " day ago");
    }
    return timeSince;
  }
}