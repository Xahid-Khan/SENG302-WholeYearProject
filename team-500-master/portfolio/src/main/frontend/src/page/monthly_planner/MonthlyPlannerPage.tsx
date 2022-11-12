import React from "react"
import styles from "./MonthlyPlannerPage.module.css"
import {PageLayout} from "../../component/layout/PageLayout"
import {MonthlyPlannerPageStoreProvider} from "./store/MonthlyPlannerPageStoreProvider";
import {MonthlyPlannerPageContent} from "./component/MonthlyPlannerPageContent";
import {MonthlyPlannerPageStore} from "./store/MonthlyPlannerPageStore";
import {ToasterRoot} from "../../component/toast/ToasterRoot";

/**
 * The root of the MonthlyPlannerPage. This does a few jobs:
 * 1. Construct the MonthlyPlannerPageStore and wrap the whole page in a Provider so it can be used by children.
 * 2. Wrap the entire page in a PageLayout.
 * 3. Place the PageContent component inside that Layout component.
 */
export const MonthlyPlannerPage: React.FC = () => {
    return (
        <MonthlyPlannerPageStoreProvider value={new MonthlyPlannerPageStore()}>
            <ToasterRoot>
                <PageLayout>
                    <div className={styles.monthlyPlannerPage}>
                        <h1>Welcome to the Monthly Planner page!</h1>

                        <div className="raised-card" style={{padding: 20}}>
                            <MonthlyPlannerPageContent/>
                        </div>
                    </div>
                </PageLayout>
            </ToasterRoot>
        </MonthlyPlannerPageStoreProvider>
    )
}