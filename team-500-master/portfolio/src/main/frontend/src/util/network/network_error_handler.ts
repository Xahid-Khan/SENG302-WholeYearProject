/**
 * Helper functions for interpreting errors encountered during network activities and wrapping them in `PortfolioNetworkError`s
 */
import {PortfolioNetworkError} from "./PortfolioNetworkError";

export interface ErrorResponseHandlerContext {
    primaryContext: string  // e.g. 'fetch project'
    secondaryContext?: string  // e.g. 'Please try again' or 'Please reload the page'
}

export const handleErrorResponse = async (res: Response, context: ErrorResponseHandlerContext): Promise<never> => {
    let errorMessage: string | undefined = undefined
    try {
        errorMessage = await res.text()
    }
    catch (e) {}

    throw new PortfolioNetworkError(
        `Failed to ${context.primaryContext}.${context.secondaryContext !== undefined ? ` ${context.secondaryContext}` : ''}`,
        {
            ...context,
            errorMessage: errorMessage ?? res.statusText,
            statusCode: res.status
        }
    )
}