import React, {useEffect} from "react";
import {observer} from "mobx-react-lite";
import {
    Box, Button,
    Divider,
    IconButton, List,
    ListSubheader,
    MenuItem,
    Popover, TextField, Tooltip,
    Typography
} from "@mui/material";
import {MessageListItem} from "./MessageListItem";
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import SendIcon from '@mui/icons-material/Send';
import {getAPIAbsolutePath} from "../../util/RelativePathUtil";
import {getUserNamesList, GroupAvatar} from "./GroupAvatar";

interface IMessageListProps{
    open: boolean
    onClose: () => void
    conversation: any
    chats: any
    backButtonCallback: (event: React.MouseEvent<HTMLElement>) => void
    messages: any
    setMessages: (messages: any) => any
}

export const MessageList: React.FC<IMessageListProps> = observer((props: IMessageListProps) => {

    const globalUrlPathPrefix = localStorage.getItem("globalUrlPathPrefix");
    const userId = localStorage.getItem("userId");
    const username = localStorage.getItem("username");

    const [message, setMessage] = React.useState("");
    const [selectedMessageId, setSelectedMessageId] = React.useState("");

    const openDeleteBox = Boolean(selectedMessageId)

    const getMessages = async () => {
        const messages = await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages/${props.conversation.conversationId}`), {
                method: 'GET'
            }
        )
        return messages.json()
    }

    const fetchAndSetMessages = async() => {
        await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages/read/${props.conversation.conversationId}`), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const conversations = await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages`), {
                method: 'GET'
            }
        )
        if (props.conversation != undefined) {
            getMessages().then((result) => {
                props.setMessages(result)
            })
        }
    }

    useEffect(() => {
        fetchAndSetMessages();
    }, [props.conversation, props.chats])

    const messages_items = () =>
        props.messages.map((contract: any) =>
            <MessageListItem
                key={contract.messageId}
                contract={contract}
                isGroupChat={props.conversation?.users.length > 2}
                messageButtonCallback={handleMessageClick}
            />
        )

    const no_messages_item = () => {
        return (
            <MenuItem disabled style={{whiteSpace: 'normal', opacity: 1}} sx={{pt: 10, pb: 10}}>
                <Typography variant="body1">Looks like you have no messages.</Typography>
            </MenuItem>
        )
    }

    const validMessage = (message: string): boolean => {
        return message != null && message.trim() !== ''
    }

    const handleSendClick = async() =>{
        if(validMessage(message)) {
            await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages/${props.conversation.conversationId}`), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    "messageContent": message,
                    "sentBy": parseInt(userId),
                    "senderName": username
                })
            });
            fetchAndSetMessages();
            setMessage("");
        }
    }

    useEffect(() => {
        //add event listener for live updating
        window.addEventListener('messages', fetchAndSetMessages);
        return () => {
            window.removeEventListener('messages', fetchAndSetMessages);
        };
    }, [])

    const handleMessageChange = (event: React.ChangeEvent<HTMLInputElement>) =>{
        setMessage(event.target.value)
    }

    const handleMessageKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) =>{
        if(event.key === 'Enter'){
            handleSendClick()
        }
    }

    const handleMessageClick = (event: React.MouseEvent<HTMLElement>, id: string) =>{
        setSelectedMessageId(id);
    }

    const handleDeleteMessageClick = async() =>{
        await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages/${props.conversation.conversationId}`), {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: selectedMessageId
        });
        setSelectedMessageId("");
        fetchAndSetMessages();
    }

    const handleCancelMessageClick = () =>{
        setSelectedMessageId("");
    }


    const sendMessageBox = () => {
        return <Box sx={{
            p: 1,
            flexGrow: 1,
            display: "flex",
            alignItems: "flex-end",
            textAlign: "center",
            justifyContent: "space-between",
            //Sticks to bottom of scroll window
            position: 'sticky',
            bottom: 0,
            backgroundColor: 'white',
        }}>
            <TextField
                value={message}
                multiline
                maxRows={3}
                label={"message"}
                hiddenLabel
                variant={"outlined"}
                size={"small"}
                onChange={handleMessageChange}
                onKeyDown={handleMessageKeyDown}
                sx={{flexGrow: 1}}/>

            <IconButton onClick={handleSendClick} disabled={!validMessage(message)}>
                <SendIcon color={validMessage(message) ? "primary" : 'disabled'}/>
            </IconButton>
        </Box>;
    }

    const deleteMessageBox = () => {
        return (
            <Box sx={{
                p: 1,
                flexGrow: 1,
                display: "flex",
                alignItems: "center",
                textAlign: "center",
                justifyContent: "space-evenly",
                //Sticks to bottom of scroll window
                position: 'sticky',
                bottom: 0,
                backgroundColor: 'white',
            }}>
                <Button variant="contained" onClick={handleCancelMessageClick}>Cancel</Button>
                <Button variant="contained" color={"error"} onClick={handleDeleteMessageClick}>Delete</Button>
            </Box>
        )
    }

    return (
        <Popover
            // Adapted from https://mui.com/material-ui/react-menu/
            anchorEl={document.getElementById('chats-list-button')}
            id="messages-menu"
            open={props.open}
            onClose={() =>{
                setSelectedMessageId("");
                props.onClose()
            }}
            PaperProps={{sx: {maxHeight: 0.5, maxWidth: 0.3, minWidth: "300px"}}}
            transformOrigin={{horizontal: "right", vertical: "top"}}
            anchorOrigin={{horizontal: "right", vertical: "bottom"}}
        >
            <List sx={{minHeight: 300}}>
                <ListSubheader sx={{pt: 1, pb: 1, pr: 0, pl: 0}}>
                    <Box sx={{
                        flexGrow: 1,
                        display: "flex",
                        alignItems: "center",
                        textAlign: "center",
                    }}>
                        <IconButton onClick={props.backButtonCallback}>
                            <ChevronLeftIcon/>
                        </IconButton>
                        {props.conversation ? (
                            <>
                                <GroupAvatar users={props.conversation.users}/>
                                <Tooltip title={getUserNamesList(props.conversation.users)}>
                                    <Typography noWrap>{getUserNamesList(props.conversation.users)}</Typography>
                                </Tooltip>
                            </>
                            ) : ""}
                    </Box>
                </ListSubheader>
                <Divider/>
                {props.messages.length === 0 ? no_messages_item() : messages_items()}
            </List>
            <Divider/>
            {openDeleteBox ? deleteMessageBox() : sendMessageBox()}
        </Popover>
    )
})