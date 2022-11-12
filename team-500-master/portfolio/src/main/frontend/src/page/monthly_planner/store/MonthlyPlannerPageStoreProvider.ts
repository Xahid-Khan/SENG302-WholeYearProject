import {createContext, useContext} from "react";
import {MonthlyPlannerPageStore} from "./MonthlyPlannerPageStore";

const MonthlyPlannerPageStoreContext = createContext<MonthlyPlannerPageStore | null>(null)

export const MonthlyPlannerPageStoreProvider = MonthlyPlannerPageStoreContext.Provider

export const useMonthlyPlannerPageStore = () => {
    const store = useContext(MonthlyPlannerPageStoreContext)
    if (store === null) {
        throw new Error("useMonthlyPlannerPageStore must be used in a component that is wrapped in MonthlyPlannerPageStoreProvider.")
    }
    return store
}