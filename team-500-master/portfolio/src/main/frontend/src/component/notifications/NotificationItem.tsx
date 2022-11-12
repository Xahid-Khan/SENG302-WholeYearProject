import {Box, MenuItem, Typography} from "@mui/material";
import React from "react";
import {observer} from "mobx-react-lite";

interface INotificationItemProps {
    description: string
    from: string
    time: Date
}

export const NotificationItem: React.FC<INotificationItemProps> = observer((props: INotificationItemProps) => {

    const timeFormat = (timestamp: Date) => {
        const today = new Date()
        const date = new Date(timestamp)
        if( date.getFullYear() === today.getFullYear() &&
            date.getMonth() === today.getMonth() &&
            date.getDate() === today.getDate()){
            return date.toLocaleTimeString()
        }
        return date.toLocaleDateString("en-UK")
    }

    return (
        <MenuItem disabled style={{whiteSpace: 'normal', opacity: 1}}>
            <Box  sx={{flexGrow: 1, display: "flex", flexDirection: "column"}}>
                <Box sx={{display: "flex", justifyContent: "space-between"}}>
                    <Box sx={{flexGrow: 1}}><Typography variant="subtitle2">{timeFormat(props.time)}</Typography></Box>
                    <Box sx={{flexGrow: 0}}><Typography variant="subtitle1">{props.from}</Typography></Box>
                </Box>
                <Typography variant="body2">{props.description}</Typography>
            </Box>
        </MenuItem>

    )
});