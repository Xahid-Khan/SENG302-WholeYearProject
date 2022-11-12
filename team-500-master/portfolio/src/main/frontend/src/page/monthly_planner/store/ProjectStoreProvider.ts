import {createContext, useContext} from "react";
import {ProjectStore} from "./ProjectStore";

const ProjectStoreContext = createContext<ProjectStore | null>(null)

export const ProjectStoreProvider = ProjectStoreContext.Provider

export const useProjectStore = () => {
    const store = useContext(ProjectStoreContext)
    if (store === null) {
        throw new Error("useProjectStore must be used in a component that is wrapped in MonthlyPlannerPageStoreProvider.")
    }
    return store
}