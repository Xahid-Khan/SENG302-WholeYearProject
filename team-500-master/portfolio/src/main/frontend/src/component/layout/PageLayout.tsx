import React from "react"
import styles from "./PageLayout.module.css"

/**
 * Component that provides the basic layout elements for a page.
 */
export const PageLayout: React.FC = ({children}) => {
    return (
        <div className={styles.pageLayout}>
            <main>
                {children}
            </main>
        </div>
    )
}