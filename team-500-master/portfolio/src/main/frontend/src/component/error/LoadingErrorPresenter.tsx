import React from "react";
import {
    LoadingDone,
    LoadingError,
    LoadingNotYetAttempted,
    LoadingPending,
    LoadingStatus
} from "../../util/network/loading_status";
import {PortfolioNetworkError} from "../../util/network/PortfolioNetworkError";

interface LoadingStatusPresenterProps {
    loadingStatus: LoadingStatus
    onRetry?: VoidFunction
}
/**
 * A handy component that formats and presents loading status errors to the user in a readable format,
 * presenting a retry button if an onRetry callback is provided.
 *
 * @param props.loadingStatus to present to the user
 * @param props.onRetry callback to trigger a 'retry' of the loading event
 */
export const LoadingErrorPresenter: React.FC<LoadingStatusPresenterProps> = (props) => {
    const {loadingStatus, onRetry} = props

    if (loadingStatus instanceof LoadingDone || loadingStatus instanceof LoadingPending) {
        throw new Error("LoadingErrorPresenter should not be used with a 'done' or 'pending' loadingStatus.")
    }
    else if (loadingStatus instanceof LoadingError) {
        const error = loadingStatus.error

        const retryRow = (onRetry === undefined) ? undefined : (
            <div><button onClick={onRetry}>Retry</button></div>
        )

        if (error instanceof PortfolioNetworkError) {
            return (
                <div>
                    <div>An error occurred: {error.message}</div>
                    {error.context.errorMessage ? (
                        <div>Received message: {error.context.errorMessage}</div>
                    ) : undefined}
                    {retryRow}
                </div>
            )
        }
        else {
            return (
                <div>
                    <div>An error occurred: {`${error}`}</div>
                    {retryRow}
                </div>
            )
        }
    }
    else if (loadingStatus instanceof LoadingNotYetAttempted) {
        return (
            <div>
                <div>Loading should start soon...</div>
                {(onRetry !== undefined) ? (
                    <div>If it doesn't start loading soon. Try clicking <button onClick={onRetry}>start loading</button></div>
                ) : undefined}
            </div>
        )
    }
    else {
        console.warn("LoadingErrorPresenter requires a LoadingStatus subclass, but one was not provided. Falling back by wrapping in a LoadingError")
        return <LoadingErrorPresenter {...props} loadingStatus={new LoadingError(loadingStatus)} />
    }
}