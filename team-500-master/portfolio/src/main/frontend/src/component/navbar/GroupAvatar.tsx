import React from "react";
import {observer} from "mobx-react-lite";
import {Avatar, AvatarGroup} from "@mui/material";

interface IGroupAvatarProps {
    users: any[]
}

/**
 * A Group of avatar images that overlap, and is limited to showing 2 avatars
 */
export const GroupAvatar: React.FC<IGroupAvatarProps> = observer((props: IGroupAvatarProps) => {

    const username = localStorage.getItem("username");
    const globalImagePath = localStorage.getItem("globalImagePath");

    return (
        <AvatarGroup max={2} sx={{mr: 2}} spacing={15}>
            {props.users
                .filter((user) => user.username != username)
                .map(
                (user) => <Avatar src={`//${globalImagePath}${user.id}`}/>
            )}
        </AvatarGroup>
    )
})

/**
 * Returns a comma separated list of the users names
 * @param usernames a list of usernames
 */
export const getUserNamesList = (users: any[]) => {
    const username = localStorage.getItem("username");
    return users
        .filter((user) => user.username != username)
        .map((user) => user.username)
        .join(', ')
}