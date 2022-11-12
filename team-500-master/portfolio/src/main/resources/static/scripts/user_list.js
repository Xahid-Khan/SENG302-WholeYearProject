'use strict';

/**
 * Parse the page parameters from the URL search component.
 */
const parseParameters = () => {
  const searchParams = window.location.search;

  if (!searchParams) {
    return null;
  }
  else {
    const pairs = searchParams.slice(1).split(/&/);
    const resultObj = {};
    for (const pair of pairs) {
      const [key, value] = pair.split(/=/, 2);
      resultObj[key] = value;
    }
    return resultObj;
  }
}

const reloadWithLastStoredParameters = () => {
  const lastParameters = JSON.parse(window.localStorage.getItem("user_list_last_parameters"));
  if (lastParameters !== null) {
    window.location.href = `?page=1&sortBy=${lastParameters.sortBy}&asc=${lastParameters.asc}`;
  }
};

const goToPage = (maxPage) => {
  const lastParameters = JSON.parse(
      window.localStorage.getItem("user_list_last_parameters"));
  const page = Number(document.getElementById("pageNumber").value)
  if (!isNaN(page)) {
    let baseUrl;
    if (!isNaN(Number(maxPage))) {
      // If the page is too big, then just load the max page instead.
      baseUrl = `?page=` + (page > maxPage ? maxPage : page)
    } else {
      baseUrl = `?page=${page}`;
    }
    // If we need to sort by or change ascension order, add them here
    if (lastParameters) {
      if (lastParameters.sortBy)
        baseUrl += `&sortBy=${lastParameters.sortBy}`;
      if (lastParameters.asc)
        baseUrl += `&asc=${lastParameters.asc}`;
    }
    window.location.href = baseUrl;
  }
}

(() => {
  // Executed every time the page loads.
  const params = parseParameters();
  if (params === null) {
    // The page has no parameters, default to the ones that we had last
    reloadWithLastStoredParameters();
  }
  else {
    // Page has parameters, save them.
    window.localStorage.setItem("user_list_last_parameters", JSON.stringify(params));
  }
})();