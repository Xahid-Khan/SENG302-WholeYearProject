import {Box, Divider, IconButton, ListSubheader, Menu, MenuItem, Typography} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import React from "react";
import {observer} from "mobx-react-lite";
import {ChatListItem} from "./ChatListItem";

interface IChatListProps{
    open: boolean
    onClose: () => void
    chats: any[]
    addButtonCallback: (event: React.MouseEvent<HTMLElement>) => void
    chatButtonCallback: (event: React.MouseEvent<HTMLElement>, contract: any) => void
}

/**
 * A list of ChatListItems. Contains a plus button to link to AddChatPopover
 */
export const ChatList: React.FC<IChatListProps> = observer((props: IChatListProps) => {

    const chats_items = () =>
        props.chats.map((contract: any) =>
            <ChatListItem
                key={contract.conversationId}
                contract={contract}
                clickCallback={props.chatButtonCallback}
            />
        )

    const no_chats_item = () => {
        return (
            <MenuItem disabled style={{whiteSpace: 'normal', opacity: 1}} sx={{pt: 10, pb: 10}}>
                <Typography variant="body1">Looks like you have no chats.</Typography>
            </MenuItem>
        )
    }

    return (
        <React.Fragment>
            <Menu
                anchorEl={document.getElementById('chats-list-button')}
                id="chat-menu"
                open={props.open}
                onClose={props.onClose}
                PaperProps={{sx: {maxHeight: 0.5, maxWidth: 0.3, minWidth: "300px", minHeight: 0.4}}}
                transformOrigin={{horizontal: "right", vertical: "top"}}
                anchorOrigin={{horizontal: "right", vertical: "bottom"}}
            >
                <ListSubheader>
                    <Box sx={{
                        flexGrow: 1,
                        display: "flex",
                        alignItems: "center",
                        textAlign: "center",
                        justifyContent: "space-between"
                    }}>
                        <Typography>Chats</Typography>
                        <IconButton id='add-button' onClick={props.addButtonCallback}>
                            <AddIcon></AddIcon>
                        </IconButton>
                    </Box>
                </ListSubheader>
                <Divider/>
                {props.chats.length === 0 ? no_chats_item() : chats_items()}
            </Menu>
        </React.Fragment>
    )
})