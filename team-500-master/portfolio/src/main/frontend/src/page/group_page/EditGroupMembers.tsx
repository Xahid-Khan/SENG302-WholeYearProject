import * as React from "react";
import {useEffect} from "react";

export function EditGroupMembers({viewGroupId}: any) {

    const isAdmin = window.localStorage.getItem("isAdmin") === "true"
    const userId = parseInt(window.localStorage.getItem("userId"))

    const getAllGroups = async ()  => {
        const allGroupsResponse = await fetch('api/v1/groups/all')
        return allGroupsResponse.json()
    }

    const [allGroups, setAllGroups] = React.useState([{
        "id": -1,
        "longName": "",
        "shortName": "",
        "users": []
    }])

    useEffect(() => {
        getAllGroups().then((result) => {
            setAllGroups(result)
        })
    }, [])

    const [membersAdded, setMembersAdded] = React.useState<{ [key: number]: number[] }>({})
    const [membersRemoved, setMembersRemoved] = React.useState<{ [key: number]: number[] }>({})

    const [errorMessage, setErrorMessage] = React.useState("")

    const [otherGroupViewing, setOtherGroupViewing] = React.useState([{
        "id": -1,
        "longName": "",
        "shortName": "",
        "users": []
    }])

    const [myGroup, setMyGroup] = React.useState({
        "id": -1,
        "longName": "",
        "shortName": "",
        "users": []
    })

    const [myGroupUpdate, setMyGroupUpdate] = React.useState(false)
    const [otherGroupViewingUpdate, setOtherGroupViewingUpdate] = React.useState(false)

    useEffect(() => {
        setMyGroup(allGroups.filter((item) => item.id === viewGroupId)[0])
    }, [viewGroupId, myGroupUpdate])

    useEffect(() => {
        if (otherGroupViewing === undefined || otherGroupViewing[0].id === -1) {
            setOtherGroupViewing(allGroups)
        }
    }, [otherGroupViewingUpdate, allGroups])


    const showFilter = (groupName: string) => {
        otherUsersSelected.forEach((id) => document.getElementById(`other-users-${id}`).style.backgroundColor = "transparent")
        currentGroupUsersSelected.forEach((id) => document.getElementById(`current-group-users-${id}`).style.backgroundColor = "transparent")
        setOtherUsersSelected([])
        setCurrentGroupUsersSelected([])
        setShiftClickOther([])
        setShiftClickCurrent([])
        document.getElementById("filter-groups-button").innerText = groupName;
        document.getElementById("other-users-title").innerText = groupName;
        if (groupName === "All users") {
            setOtherGroupViewing(allGroups)
        } else {
            setOtherGroupViewing(allGroups.filter((item: any) => item.shortName === groupName))
        }
    }

    const [otherUsersSelected, setOtherUsersSelected] = React.useState([])
    const [currentGroupUsersSelected, setCurrentGroupUsersSelected] = React.useState([])
    const [shiftClickOther, setShiftClickOther] = React.useState([])
    const [shiftClickCurrent, setShiftClickCurrent] = React.useState([])

    const handleOtherShiftPress = (user: any) => {
        let firstSelection
        if (shiftClickOther.length === 0 && otherUsersSelected.length === 0) {
            setShiftClickOther([user.id])
            setOtherUsersSelected([user.id])
            document.getElementById(`other-users-${user.id}`).style.backgroundColor = "#ccc"
        } else {
            if (shiftClickOther.length > 0) {
                firstSelection = document.getElementById(`other-users-${shiftClickOther[0]}`)
            } else {
                const userIdClicked = otherUsersSelected[otherUsersSelected.length - 1]
                setShiftClickOther([userIdClicked])
                firstSelection = document.getElementById(`other-users-${userIdClicked}`)
            }
            const nextSelection = document.getElementById(`other-users-${user.id}`)
            const children = document.getElementById("other-users-list").children
            let startIndex = null
            let endIndex = null
            for (let i = 0; i < children.length; i++) {
                if (children[i] === firstSelection || children[i] === nextSelection) {
                    if (startIndex === null) {
                        startIndex = i
                    } else {
                        endIndex = i
                    }
                }
            }
            if (nextSelection === firstSelection) {
                endIndex = startIndex
            }
            let newSelections = []
            for (let i = 1; i < children.length; i++) {
                if (i >= startIndex && i <= endIndex) {
                    document.getElementById(children[i].id).style.backgroundColor = "#ccc"
                    newSelections.push(parseInt(children[i].id.split('-')[2]))
                } else {
                    document.getElementById(children[i].id).style.backgroundColor = "transparent"
                }
            }
            setOtherUsersSelected(newSelections)
        }
    }

    const handleOtherUserSelect = (event: any, user: any) => {
        setErrorMessage("")
        currentGroupUsersSelected.forEach((id) => {
            document.getElementById(`current-group-users-${id}`).style.backgroundColor = "transparent"
        })
        setCurrentGroupUsersSelected([])
        setShiftClickCurrent([])
        if (event.shiftKey) {
            handleOtherShiftPress(user);
        } else {
            if (!event.ctrlKey && !event.metaKey) {
                setShiftClickOther([])
            }
            if (otherUsersSelected.filter((id) => id === user.id).length > 0) {
                if (event.ctrlKey || event.metaKey) {
                    setOtherUsersSelected(otherUsersSelected.filter((id) => id != user.id))
                    document.getElementById(`other-users-${user.id}`).style.backgroundColor = "transparent"
                } else {
                    otherUsersSelected.filter((id) => id != user.id).forEach((otherUser) => document.getElementById(`other-users-${otherUser}`).style.backgroundColor = "transparent")
                    setOtherUsersSelected(otherUsersSelected.filter((id) => id == user.id))
                }
            } else {
                if (event.ctrlKey || event.metaKey) {
                    setOtherUsersSelected([...otherUsersSelected, user.id])
                    document.getElementById(`other-users-${user.id}`).style.backgroundColor = "#ccc"
                } else {
                    otherUsersSelected.forEach((id) => {
                        document.getElementById(`other-users-${id}`).style.backgroundColor = "transparent"
                    })
                    setOtherUsersSelected([user.id])
                    document.getElementById(`other-users-${user.id}`).style.backgroundColor = "#ccc"
                }
            }
        }
    }

    const handleCurrentGroupShiftPress = (user: any) => {
        let firstSelection
        if (shiftClickCurrent.length === 0 && currentGroupUsersSelected.length === 0) {
            setShiftClickCurrent([user.id])
            setCurrentGroupUsersSelected([user.id])
            document.getElementById(`current-group-users-${user.id}`).style.backgroundColor = "#ccc"
        } else {
            if (shiftClickCurrent.length > 0) {
                firstSelection = document.getElementById(`current-group-users-${shiftClickCurrent[0]}`)
            } else {
                const userIdClicked = currentGroupUsersSelected[currentGroupUsersSelected.length - 1]
                setShiftClickCurrent([userIdClicked])
                firstSelection = document.getElementById(`current-group-users-${userIdClicked}`)
            }
            const nextSelection = document.getElementById(`current-group-users-${user.id}`)
            const children = document.getElementById("current-group-users-list").children
            let startIndex = null
            let endIndex = null
            for (let i = 0; i < children.length; i++) {
                if (children[i] === firstSelection || children[i] === nextSelection) {
                    if (startIndex === null) {
                        startIndex = i
                    } else {
                        endIndex = i
                    }
                }
            }
            if (nextSelection === firstSelection) {
                endIndex = startIndex
            }
            let newSelections = []
            for (let i = 1; i < children.length; i++) {
                if (i >= startIndex && i <= endIndex) {
                    document.getElementById(children[i].id).style.backgroundColor = "#ccc"
                    newSelections.push(parseInt(children[i].id.split('-')[3]))
                } else {
                    document.getElementById(children[i].id).style.backgroundColor = "transparent"
                }
            }
            setCurrentGroupUsersSelected(newSelections)
        }
    }

    const handleCurrentGroupUserSelect = (event: any, user: any) => {
        setErrorMessage("")
        otherUsersSelected.forEach((id) => {
            document.getElementById(`other-users-${id}`).style.backgroundColor = "transparent"
        })
        setOtherUsersSelected([])
        setShiftClickOther([])
        if (event.shiftKey) {
            handleCurrentGroupShiftPress(user);
        } else {
            if (!event.ctrlKey && !event.metaKey) {
                setShiftClickCurrent([])
            }
            if (currentGroupUsersSelected.filter((id) => id === user.id).length > 0) {
                if (event.ctrlKey || event.metaKey) {
                    setCurrentGroupUsersSelected(currentGroupUsersSelected.filter((id) => id != user.id))
                    document.getElementById(`current-group-users-${user.id}`).style.backgroundColor = "transparent"
                } else {
                    currentGroupUsersSelected.filter((id) => id != user.id).forEach((otherUser) => document.getElementById(`current-group-users-${otherUser}`).style.backgroundColor = "transparent")
                    setCurrentGroupUsersSelected(currentGroupUsersSelected.filter((id) => id == user.id))
                }
            } else {
                if (event.ctrlKey || event.metaKey) {
                    setCurrentGroupUsersSelected([...currentGroupUsersSelected, user.id])
                    document.getElementById(`current-group-users-${user.id}`).style.backgroundColor = "#ccc"
                } else {
                    currentGroupUsersSelected.forEach((id) => {
                        document.getElementById(`current-group-users-${id}`).style.backgroundColor = "transparent"
                    })
                    setCurrentGroupUsersSelected([user.id])
                    document.getElementById(`current-group-users-${user.id}`).style.backgroundColor = "#ccc"
                }
            }
        }
    }

    const addToCurrent = () => {
        let duplicate = false
        let usersToAdd: any = []
        let usersToAddIds: number[] = []
        let nonGroup: any = null
        allGroups.forEach((group: any) => {
            if (group['shortName'] === "Non Group") {
                nonGroup = group
            }
            group['users'].forEach((user: any) => {
                if (otherUsersSelected.includes(user.id) && !usersToAddIds.includes(user.id)) {
                    usersToAdd.push(user)
                    usersToAddIds.push(user.id)
                }
            })
        })
        myGroup.users.forEach((user) => {
            usersToAdd.forEach((userToAdd: any) => {
                if (user.id === userToAdd.id) {
                    duplicate = true
                    setErrorMessage("One or more of the users selected are already in this group. Please deselect these users and try again")
                }
            })
        })

        usersToAdd.forEach((user: any) => {
            nonGroup['users'].forEach((nonGroupUser: any) => {
                if (user.id === nonGroupUser.id) {
                    if (membersRemoved[nonGroup.id] === undefined) {
                        membersRemoved[nonGroup.id] = [user.id]
                    } else {
                        membersRemoved[nonGroup.id].push(user.id)
                    }
                }
            })
        })

        if (duplicate === false) {
            myGroup.users = myGroup.users.concat(usersToAdd)
            allGroups.forEach((item: any, index: number) => {
                if (item.id === myGroup.id) {
                    allGroups[index].users = myGroup.users
                }
            })
            let filteredUserIds: number[] = []
            usersToAddIds.forEach((id, index) => {
                if (membersRemoved[myGroup.id] && membersRemoved[myGroup.id].includes(id)) {
                    membersRemoved[myGroup.id] = membersRemoved[myGroup.id].slice(0, index).concat(membersRemoved[myGroup.id].slice(index + 1, membersRemoved[myGroup.id].length))
                } else {
                    filteredUserIds.push(id)
                }
            })

            if (membersAdded[myGroup.id] === undefined) {
                membersAdded[myGroup.id] = filteredUserIds
            } else {
                filteredUserIds.forEach((id) => {
                    membersAdded[myGroup.id].push(id)
                })
            }
            setMyGroupUpdate(!myGroupUpdate)
        }
        setOtherUsersSelected([])
        otherUsersSelected.forEach((id) => document.getElementById(`other-users-${id}`).style.backgroundColor = "transparent")
    }

    const copyToOther = () => {
        let duplicate = false
        let usersToAdd: any = []
        let usersToAddIds: number[] = []
        allGroups.forEach((group: any) => {
            group['users'].forEach((user: any) => {
                if (currentGroupUsersSelected.includes(user.id)) {
                    usersToAdd.push(user)
                    usersToAddIds.push(user.id)
                }
            })
        })
        if (myGroup['shortName'] === "Non Group") {
            let usersToRemoveIds: number[] = []
            currentGroupUsersSelected.forEach((id) => {
                myGroup.users.forEach((user: any, index: number) => {
                    if (user.id === id) {
                        myGroup.users = myGroup.users.slice(0, index).concat(myGroup.users.slice(index + 1, myGroup.users.length))
                        usersToRemoveIds.push(user.id)
                    }
                })
            })
            allGroups.forEach((item: any, index: number) => {
                if (item.id === myGroup.id) {
                    allGroups[index].users = myGroup.users
                }
            })
            let filteredUserIds: number[] = []
            usersToRemoveIds.forEach((id, index) => {
                if (membersAdded[myGroup.id] && membersAdded[myGroup.id].includes(id)) {
                    membersAdded[myGroup.id] = membersAdded[myGroup.id].slice(0, index).concat(membersAdded[myGroup.id].slice(index + 1, membersAdded[myGroup.id].length))
                } else {
                    filteredUserIds.push(id)
                }
            })
            if (membersRemoved[myGroup.id] === undefined) {
                membersRemoved[myGroup.id] = filteredUserIds
            } else {
                filteredUserIds.forEach((id) => {
                    membersRemoved[myGroup.id].push(id)
                })
            }
        }
        otherGroupViewing[0].users.forEach((user: any) => {
            usersToAdd.forEach((userToAdd: any) => {
                if (user.id === userToAdd.id) {
                    duplicate = true
                    setErrorMessage("One or more of the users selected are already in this group. Please deselect these users and try again")
                }
            })
        })
        if (duplicate === false) {
            otherGroupViewing[0].users = otherGroupViewing[0].users.concat(usersToAdd)
            allGroups.forEach((item: any, index: number) => {
                if (item.id === otherGroupViewing[0].id) {
                    allGroups[index].users = otherGroupViewing[0].users
                }
            })

            let filteredUserIds: number[] = []
            usersToAddIds.forEach((id, index) => {
                if (membersRemoved[otherGroupViewing[0].id] && membersRemoved[otherGroupViewing[0].id].includes(id)) {
                    membersRemoved[otherGroupViewing[0].id] = membersRemoved[otherGroupViewing[0].id].slice(0, index).concat(membersRemoved[otherGroupViewing[0].id].slice(index + 1, membersRemoved[otherGroupViewing[0].id].length))
                } else {
                    filteredUserIds.push(id)
                }
            })

            if (membersAdded[otherGroupViewing[0].id] === undefined) {
                membersAdded[otherGroupViewing[0].id] = filteredUserIds
            } else {
                filteredUserIds.forEach((id) => {
                    membersAdded[otherGroupViewing[0].id].push(id)
                })
            }
            setOtherGroupViewingUpdate(!otherGroupViewingUpdate)
        }
        currentGroupUsersSelected.forEach((id) => document.getElementById(`current-group-users-${id}`).style.backgroundColor = "transparent")
        setCurrentGroupUsersSelected([])
    }

    const addToNonGroupIfNeeded = (user: any, groupId: number) => {
        let userInAnotherGroup = false
        allGroups.forEach((group: any) => {
            if (group.id !== groupId) {
                group['users'].forEach((otherUser: any) => {
                    if (otherUser.id === user.id) {
                        userInAnotherGroup = true
                    }
                })
            }
        })
        if (userInAnotherGroup === false) {
            allGroups.forEach((group: any) => {
                if (group.shortName === "Non Group") {
                    group.users.push(user)
                    allGroups.forEach((otherGroup: any) => {
                        if (otherGroup['shortName'] === "Non Group") {
                            if (membersAdded[otherGroup.id] === undefined) {
                                membersAdded[otherGroup.id] = [user.id]
                            } else {
                                membersAdded[otherGroup.id].push(user.id)
                            }
                        }
                    })
                }
            })
        }
    }

    const removeFromGroup = () => {
        let usersToRemoveIds: number[] = []
        if (currentGroupUsersSelected.length > 0) {
            currentGroupUsersSelected.forEach((id) => {
                myGroup.users.forEach((user: any, index: number) => {
                    if (user.id === id) {
                        myGroup.users = myGroup.users.slice(0, index).concat(myGroup.users.slice(index + 1, myGroup.users.length))
                        addToNonGroupIfNeeded(user, myGroup.id)
                        usersToRemoveIds.push(user.id)
                    }
                })
            })
            allGroups.forEach((item: any, index: number) => {
                if (item.id === myGroup.id) {
                    allGroups[index].users = myGroup.users
                }
            })
            let filteredUserIds: number[] = []
            usersToRemoveIds.forEach((id, index) => {
                if (membersAdded[myGroup.id] && membersAdded[myGroup.id].includes(id)) {
                    membersAdded[myGroup.id] = membersAdded[myGroup.id].slice(0, index).concat(membersAdded[myGroup.id].slice(index + 1, membersAdded[myGroup.id].length))
                } else {
                    filteredUserIds.push(id)
                }
            })

            if (membersRemoved[myGroup.id] === undefined) {
                membersRemoved[myGroup.id] = filteredUserIds
            } else {
                filteredUserIds.forEach((id) => {
                    membersRemoved[myGroup.id].push(id)
                })
            }
            setCurrentGroupUsersSelected([])
            setMyGroupUpdate(!myGroupUpdate)
        } else {
            otherUsersSelected.forEach((id) => {
                otherGroupViewing[0].users.forEach((user: any, index: number) => {
                    if (user.id === id) {
                        otherGroupViewing[0].users = otherGroupViewing[0].users.slice(0, index).concat(otherGroupViewing[0].users.slice(index + 1, otherGroupViewing[0].users.length))
                        addToNonGroupIfNeeded(user, otherGroupViewing[0].id)
                        usersToRemoveIds.push(user.id)
                    }
                })
            })
            allGroups.forEach((item: any, index: number) => {
                if (item.id === otherGroupViewing[0].id) {
                    allGroups[index].users = otherGroupViewing[0].users
                }
            })
            let filteredUserIds: number[] = []
            usersToRemoveIds.forEach((id, index) => {
                if (membersAdded[otherGroupViewing[0].id] && membersAdded[otherGroupViewing[0].id].includes(id)) {
                    membersAdded[otherGroupViewing[0].id] = membersAdded[otherGroupViewing[0].id].slice(0, index).concat(membersAdded[otherGroupViewing[0].id].slice(index + 1, membersAdded[otherGroupViewing[0].id].length))
                } else {
                    filteredUserIds.push(id)
                }
            })

            if (membersRemoved[otherGroupViewing[0].id] === undefined) {
                membersRemoved[otherGroupViewing[0].id] = filteredUserIds
            } else {
                filteredUserIds.forEach((id) => {
                    membersRemoved[otherGroupViewing[0].id].push(id)
                })
            }
            setOtherUsersSelected([])
            setOtherGroupViewingUpdate(!otherGroupViewingUpdate)
        }
    }

    const handleMemberEditSubmit = async () => {
        let response
        /* Taken from https://stackoverflow.com/questions/34913675/how-to-iterate-keys-values-in-javascript */
        for (const [key, value] of Object.entries(membersRemoved)) {
            response = await fetch(`api/v1/groups/${key}/delete-members`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(value)
            });
        }
        for (const [key, value] of Object.entries(membersAdded)) {
            response = await fetch(`api/v1/groups/${key}/add-members`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(value)
            });
        }
        document.getElementById("modal-edit-group-members-open").style.display = "none"
        window.location.reload()
    }

    const handleCancel = async () => {
        document.getElementById("modal-edit-group-members-open").style.display = "none";
    }

    if (document.getElementById("group-edit-members-confirm")) {
        let oldConfirm = document.getElementById("group-edit-members-confirm")
        let newConfirm = oldConfirm.cloneNode(true)
        oldConfirm.parentNode.replaceChild(newConfirm, oldConfirm);
        let oldCancel = document.getElementById("group-edit-members-cancel")
        let newCancel = oldCancel.cloneNode(true)
        oldCancel.parentNode.replaceChild(newCancel, oldCancel);
        let oldX = document.getElementById("group-edit-members-confirm")
        let newX = oldX.cloneNode(true)
        oldX.parentNode.replaceChild(newX, oldX);
        document.getElementById("group-edit-members-confirm").addEventListener("click", () => handleMemberEditSubmit())
        document.getElementById("group-edit-members-cancel").addEventListener("click", () => handleCancel())
        document.getElementById("group-edit-members-x").addEventListener("click", () => handleCancel())
    }

    const openRemoveModal = () => {
        document.getElementById("modal-delete-open").style.display = "block"
        document.getElementById("modal-delete-confirm").addEventListener("click", () => {
            document.getElementById("modal-delete-open").style.display = "none"
            removeFromGroup()
        })
        document.getElementById("modal-delete-cancel").addEventListener("click", () => {
            document.getElementById("modal-delete-open").style.display = "none"
        })
        document.getElementById("modal-delete-x").addEventListener("click", () => {
            document.getElementById("modal-delete-open").style.display = "none"
        })
    }

    const getOtherViewingUsers = (): any => {

        let otherGroupViewingNoDuplicates: any = []
        otherGroupViewing.forEach((group) => {
            group['users'].forEach((user) => {
                let duplicate = false
                otherGroupViewingNoDuplicates.forEach((otherUser: any) => {
                    if (otherUser.id === user.id) {
                        duplicate = true
                    }
                })
                let inMyGroup = false
                if (document.getElementById("filter-groups-button").innerText === "All users") {
                    myGroup.users.forEach((myUser) => {
                        if (myUser.id === user.id) {
                            inMyGroup = true
                        }
                    })
                }
                if (duplicate === false && inMyGroup === false) {
                    otherGroupViewingNoDuplicates.push(user)
                }
            })
        })
        return otherGroupViewingNoDuplicates
    }

    const formatRoles = (roles: any) => {
        let toReturn: string = ""
        roles.forEach((role: string) => {
            if (role === "COURSE_ADMINISTRATOR") {
                if (toReturn === "") {
                    toReturn += "Course Admin"
                } else {
                    toReturn += ", Course Admin"
                }
            } else {
                if (toReturn === "") {
                    toReturn += role[0] + role.slice(1, role.length).toLowerCase()
                } else {
                    toReturn += ", " + role[0] + role.slice(1, role.length).toLowerCase()
                }
            }
        })
        return toReturn
    }

    const isEditingSelf = () => {
        let found = false;
        otherUsersSelected.forEach((id) => {
            if (id === userId) {
                found = true
            }
        })
        currentGroupUsersSelected.forEach((id) => {
            if (id === userId) {
                found = true
            }
        })
        return found
    }

    return (
        <div>
            {myGroup ?
                <div className={"edit-group-members-container"}>
                    <div className={"current-group"}>
                        <h2>Current Group</h2>
                        <div className={'raised-card group'} id={`current-group-members-${myGroup['id']}`}>
                            <div className={"group-header"}>
                                <h2 className={'group-name-short'}>{myGroup['shortName']}</h2>
                            </div>
                            <h3 className={'group-name-long'}>{myGroup['longName']}</h3>
                            <div className={"user-list-table"}>
                                <div className={"table groups-table"} id={"current-group-users-list"}>
                                    <div className={"groups-header"}>
                                        <div className="tableCell"><b>Name</b></div>
                                        <div className="tableCell"><b>Username</b></div>
                                        <div className="tableCell"><b>Alias</b></div>
                                        <div className="tableCell"><b>Roles</b></div>
                                    </div>
                                    {myGroup['users'].map((user: any) => (
                                        <div className="groups-row" id={`current-group-users-${user.id}`} key={user.id}
                                             onClick={(event => handleCurrentGroupUserSelect(event, user))}>
                                            <div className="tableCell">{user['firstName']} {user['lastName']}</div>
                                            <div className="tableCell">{user['username']}</div>
                                            <div className="tableCell">{user['nickName']}</div>
                                            <div className="tableCell">{formatRoles(user['roles'])}</div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div>
                        <div className={"edit-group-members-buttons"}>
                            <button className={"edit-group-members-button"} disabled={otherUsersSelected.length === 0 || myGroup.shortName === "Non Group"}
                                    onClick={() => addToCurrent()}><span className="material-icons"
                                                                         style={{fontSize: 14}}>arrow_back</span> Add to
                                current
                            </button>
                            <button className={"edit-group-members-button"}
                                    disabled={currentGroupUsersSelected.length === 0 || document.getElementById("filter-groups-button").innerText === "All users"}
                                    onClick={() => copyToOther()}>Copy to other <span className="material-icons"
                                                                                      style={{fontSize: 14}}>arrow_forward</span>
                            </button>
                            <button className={"edit-group-members-button"}
                                    disabled={((currentGroupUsersSelected.length === 0 || myGroup.shortName === "Non Group") && (otherUsersSelected.length === 0 || document.getElementById("filter-groups-button").innerText === "All users")) || (!isAdmin && myGroup.shortName === "Teachers" && (isEditingSelf()))}
                                    onClick={() => openRemoveModal()}>Remove from group
                            </button>
                        </div>
                        <div className={"edit-group-members-error"}>{errorMessage}</div>
                    </div>
                    <div className={"other-group-users"}>
                        <div className={"other-groups-users-header"}>
                            <div className={"filter-groups"}>
                                <div className={"filter-groups-dropdown"}>
                                    <div className={"filter-groups-header"}>
                                        <button className={"filter-groups-button"} id={"filter-groups-button"}>All
                                            users
                                        </button>
                                    </div>
                                    <div className={"filter-groups-options"} id={"filter-groups-options"}>
                                        <button className={"filter-option-button"}
                                                onClick={() => showFilter("All users")}>All users
                                        </button>
                                        {allGroups.filter((item: any) => item.id != myGroup.id && item.longName !== "Users without a group").map((item: any) => (
                                            <button className={"filter-option-button"} key={item.id}
                                                    onClick={() => showFilter(item['shortName'])}>{item['shortName']}</button>
                                        ))}
                                    </div>
                                </div>
                            </div>
                            <div className={"filter-label"}>Select Group:</div>
                            <div>
                                <h2>Other Users</h2>
                            </div>
                        </div>
                        <div className={'raised-card group'} id={`current-group-members-${myGroup['id']}`}>
                            <h2 id={"other-users-title"}>All Users</h2>
                            <div className={"user-list-table"}>
                                <div className={"table groups-table"} id={"other-users-list"}>
                                    <div className={"groups-header"}>
                                        <div className="tableCell"><b>Name</b></div>
                                        <div className="tableCell"><b>Username</b></div>
                                        <div className="tableCell"><b>Alias</b></div>
                                        <div className="tableCell"><b>Roles</b></div>
                                    </div>
                                    {getOtherViewingUsers().map((user: any) => (
                                        <div className="groups-row" id={`other-users-${user.id}`} key={user.id}
                                             onClick={(event => handleOtherUserSelect(event, user))}>
                                            <div className="tableCell">{user['firstName']} {user['lastName']}</div>
                                            <div className="tableCell">{user['username']}</div>
                                            <div className="tableCell">{user['nickName']}</div>
                                            <div className="tableCell">{formatRoles(user['roles'])}</div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                : ""}
        </div>
    );
}