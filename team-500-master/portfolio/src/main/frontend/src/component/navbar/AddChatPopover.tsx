import {
    Avatar, Box, Button, Chip,
    Divider,
    IconButton,
    List,
    ListSubheader, MenuItem,
    Popover,
    TextField,
    Typography
} from "@mui/material";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import React, {useEffect} from "react";
import {observer} from "mobx-react-lite";
import {getAPIAbsolutePath} from "../../util/RelativePathUtil";

interface IUserListProps{
    open: boolean
    onClose: () => void
    chats: any[]
    backButtonCallback: () => void
    chatButtonCallback: (event: React.MouseEvent<HTMLElement>, contract: any) => void
}

/**
 * A popover component for adding conversations.
 * Contains a list of users, a search bar to filter the list and a create button.
 * Also contains a series of deletable chips for each selected user.
 */
export const AddChatPopover: React.FC<IUserListProps> = observer((props: IUserListProps) => {

    const globalUrlPathPrefix = localStorage.getItem("globalUrlPathPrefix");
    const globalImagePath = localStorage.getItem("globalImagePath");

    const userId = localStorage.getItem("userId");

    const [search, setSearch] = React.useState("");

    const [users, setUsers] = React.useState([]);
    const [selectedUsers, setSelectedUsers] = React.useState([]);

    let conversation: any = undefined;

    const fetchUsers = async () => {
        const messages = await fetch(getAPIAbsolutePath(globalUrlPathPrefix, "messages/all-users"));
        return messages.json();
    }

    useEffect(() => {
        fetchUsers().then((result) => {
            let users = [];
            if (result['userIds'] !== undefined) {
                for (let i = 0; i < result.userIds.length; i++) {
                    users.push({"userId": result.userIds[i], "username": result.usernames[i]})
                }
            }
            setUsers(users);
        })
    }, [])

    const filteredUsers = () => {
        return users.filter((user: any) => user.username.toLowerCase().includes(search.toLowerCase()) && user.userId !== userId && !selectedUsers.includes(user))
    }

    const users_items = () =>
        filteredUsers()
            .map((user: any) =>
            <MenuItem onClick={(event) => handleContactAdd(user)}>
                <Box
                    key={user.id}
                    sx={{
                        display: "flex",
                        alignItems: "center",
                        textAlign: "center",
                    }}
                >
                    <Avatar sx={{mr: 2}} src={`//${globalImagePath}${user.id}`}/>
                    <Typography>{user.username}</Typography>
                </Box>
            </MenuItem>
        )

    const no_users_item = () => {
        return (
            <MenuItem disabled style={{whiteSpace: 'normal', opacity: 1}} sx={{pt: 5, pb: 5}}>
                <Typography variant="body1">No available users.</Typography>
            </MenuItem>
        )
    }

    const handleChipDelete = (id: number) => {
        setSelectedUsers(selectedUsers.filter((user) => {user.id !== id}));
    }

    const chips = () =>
        selectedUsers.map((user: any) =>
            <Chip
                variant="outlined"
                size="small"
                onDelete={() => handleChipDelete(user.id)}
                avatar={<Avatar src={`//${globalImagePath}${user.id}`} />}
                label={user.username}
            />
        )

    const handleContactAdd = (user: any) => {
        if(!selectedUsers.includes(user)) {
            setSelectedUsers(selectedUsers.concat(user));
        }
    }

    const newConversation = () => {
        const ids = selectedUsers.map((user) => parseInt(user.userId));
        ids.push(parseInt(userId));
        return ids;
    }

    const groupAlreadyExists = () => {
        conversation = undefined
        let alreadyExists = false
        const y = selectedUsers.map((user: any) => parseInt(user.userId))
        y.push(parseInt(userId))
        for(let chat of props.chats) {
            const x = chat.users.map((user: any) => user.id)
            if(x.every((item: number) => y.includes(item)) && y.every((item: number) => x.includes(item))){
                alreadyExists = true;
                conversation = chat
            }
        }
        return alreadyExists
    }

    const handleCreateClick = async(e: React.MouseEvent<HTMLElement>) => {
        setSearch("")
        if(groupAlreadyExists()){
            props.chatButtonCallback(e, conversation)
            return
        }

        await fetch(getAPIAbsolutePath(globalUrlPathPrefix, `messages`), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(
                newConversation()
            )
        });
        props.backButtonCallback();
        setSelectedUsers([])
    }

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) =>{
        setSearch(event.target.value)
    }

    return (
        <Popover
            // Adapted from https://mui.com/material-ui/react-menu/
            anchorEl={document.getElementById('chats-list-button')}
            id="messages-menu"
            open={props.open}
            onClose={() =>{
                props.onClose()
            }}
            PaperProps={{sx: {maxHeight: 0.8, maxWidth: 0.3, minWidth: "300px", minHeight: 0.4}}}
            transformOrigin={{horizontal: "right", vertical: "top"}}
            anchorOrigin={{horizontal: "right", vertical: "bottom"}}
        >
            <List>
                <ListSubheader sx={{pt: 1, pb: 1, pr: 0, pl: 0}}>
                    <Box sx={{
                        p: 1,
                        display: "flex",
                        flexDirection: 'column'
                    }}>
                        <Box sx={{
                            flexGrow: 1,
                            display: "flex",
                            alignItems: "center",
                            textAlign: "center",
                        }}>
                            <IconButton onClick={props.backButtonCallback}>
                                <ChevronLeftIcon></ChevronLeftIcon>
                            </IconButton>
                            <Typography>Add Conversation</Typography>
                        </Box>
                        <TextField label="Search" variant="standard" onChange={handleSearchChange}/>
                        <Box sx={{display: 'flex', flexWrap: 'wrap', mt:1}}>{chips()}</Box>
                        <Box sx={{pt: 1, display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-end'}}>
                            <Typography sx={{color: 'red', fontSize: '0.8em', mr: 1}} display={groupAlreadyExists() ? 'block' : 'none'}>Chat already exists. Open it?</Typography>
                            <Button
                                id={'create-group-button'}
                                disabled={selectedUsers?.length == 0}
                                size="small"
                                variant="contained"
                                color={"success"}
                                onClick={handleCreateClick}>{groupAlreadyExists() ? 'Open' : 'Create'}
                            </Button>
                        </Box>
                    </Box>
                </ListSubheader>
                <Divider/>
                {filteredUsers().length === 0 ? no_users_item() : users_items()}
            </List>
        </Popover>
    )
})