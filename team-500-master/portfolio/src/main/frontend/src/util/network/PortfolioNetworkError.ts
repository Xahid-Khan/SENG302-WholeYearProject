import {ErrorResponseHandlerContext} from "./network_error_handler";

export interface PortfolioNetworkErrorContext extends ErrorResponseHandlerContext {
    errorMessage?: string
    statusCode?: number
}

/**
 * Error class for network errors (with more detailed information about the error)
 */
export class PortfolioNetworkError extends Error {
    readonly context: PortfolioNetworkErrorContext

    constructor(message: string, context: PortfolioNetworkErrorContext) {
        super(message);
        this.context = context
    }
}