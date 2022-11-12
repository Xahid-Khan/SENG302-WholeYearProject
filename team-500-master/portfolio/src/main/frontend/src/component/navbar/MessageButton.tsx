import React, {useEffect} from "react";
import {observer} from "mobx-react-lite";
import {Badge, Box, IconButton} from "@mui/material";
import {ChatList} from "./ChatList";
import {MessageList} from "./MessageList";
import MailIcon from '@mui/icons-material/Mail';
import {AddChatPopover} from "./AddChatPopover";
import {getAPIAbsolutePath} from "../../util/RelativePathUtil";

/**
 * A button that accesses message UIs. Is the parent component for all chat/message related popovers.
 */
export const MessageButton: React.FC = observer(() => {

    const userId = localStorage.getItem("userId");
    const globalUrlPathPrefix = localStorage.getItem("globalUrlPathPrefix");

    const [chats, setChats] = React.useState([]);


    const [messages, setMessages] = React.useState([]);

    const fetchChats = async () => {
        const conversations = await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages`), {
                method: 'GET'
            }
        )
        return conversations.json()
    }

    const fetchAndSetChats = () => {
        fetchChats().then((result) => {
            setChats(result)
        })
    }

    useEffect(() => {
        fetchAndSetChats();
    }, [])


    const [conversation, setConversation] = React.useState(undefined);

    // Adapted from https://mui.com/material-ui/react-menu/
    //the element that was last clicked on
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleChatClick = async(event: React.MouseEvent<HTMLElement>, contract: any) => {
        handleClick(event)

        setConversation(contract)
    };

    const handleBackClick = () => {
        setAnchorEl(document.getElementById('chats-list-button'));
        setConversation(undefined)
        fetchAndSetChats();
        setMessages([])
    }

    const getNumUnreadChats = () => {
        let i = 0;
        chats.forEach((chat: any) => { if(!chat.userHasReadMessages.includes(parseInt(userId))){i++}});
        return i;
    }

    //uses the last clicked element to determine which menu to open
    const openChats = anchorEl?.id === 'chats-list-button';
    const openAdd = anchorEl?.id === 'add-button';
    const openChat = anchorEl?.id.startsWith('chat-button') || anchorEl?.id === 'create-group-button';


    useEffect(() => {
        fetchAndSetChats();
    }, [openChat])


    useEffect(() => {
        //add event listener for live updating
        window.addEventListener('messages', fetchAndSetChats);
        return () => {
            window.removeEventListener('messages', fetchAndSetChats);
        };
    }, [])

    return (
        <React.Fragment>
            <Box sx={{display: 'flex', alignItems: 'center', textAlign: 'center'}}>
                <IconButton
                    // Adapted from https://mui.com/material-ui/react-menu/
                    id={'chats-list-button'}
                    onClick={handleClick}
                    size="small"
                    sx={{ml: 2}}
                    aria-controls={openChats ? 'chat-menu' : undefined}
                    aria-haspopup="true"
                    aria-expanded={openChats ? 'true' : undefined}
                >
                    <Badge badgeContent={getNumUnreadChats()} color="primary">
                        <MailIcon sx={{width: 32, height: 32}}></MailIcon>
                    </Badge>
                </IconButton>
            </Box>

            <ChatList
                open={openChats}
                onClose={handleClose}
                chats={chats}
                addButtonCallback={handleClick}
                chatButtonCallback={handleChatClick}
            />

            <MessageList
                open={openChat}
                onClose={handleClose}
                conversation={conversation}
                chats={chats}
                backButtonCallback={handleBackClick}
                setMessages={setMessages}
                messages={messages}
            />

            <AddChatPopover
                open={openAdd}
                onClose={handleClose}
                chats={chats}
                backButtonCallback={handleBackClick}
                chatButtonCallback={handleChatClick}
            />

        </React.Fragment>
    )
})