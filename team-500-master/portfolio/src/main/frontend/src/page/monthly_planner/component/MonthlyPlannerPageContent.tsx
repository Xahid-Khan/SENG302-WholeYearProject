import React from "react";
import {observer} from "mobx-react-lite";
import {useMonthlyPlannerPageStore} from "../store/MonthlyPlannerPageStoreProvider";
import {LoadingDone, LoadingPending} from "../../../util/network/loading_status";
import {LoadingErrorPresenter} from "../../../component/error/LoadingErrorPresenter";
import {ProjectStoreProvider} from "../store/ProjectStoreProvider";
import {ProjectMonthCalendar} from "./ProjectMonthCalendar";

export const MonthlyPlannerPageContent: React.FC = observer(() => {
    const pageStore = useMonthlyPlannerPageStore()

    if (pageStore.projectLoadingStatus instanceof LoadingDone) {
        return (
            <ProjectStoreProvider value={pageStore.project}>
                <ProjectMonthCalendar/>
            </ProjectStoreProvider>
        )
    }
    else if (pageStore.projectLoadingStatus instanceof LoadingPending) {
        return (
            <p>Loading...</p>
        )
    }
    else {
        return <LoadingErrorPresenter loadingStatus={pageStore.projectLoadingStatus} onRetry={() => pageStore.fetchProject()}/>
    }
})

