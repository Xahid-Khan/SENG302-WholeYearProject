import * as React from "react";
import {useEffect} from "react";
import {GroupSettingsModal} from "./GroupSettingsModal";


const getAllGroups = async () => {
  const allGroupsResponse = await fetch('api/v1/groups/all')
  return allGroupsResponse.json()
}

const deleteGroup = async (id: number, groups: any) => {
  let usersToRemoveIds: any = []
  groups.forEach((group: any) => {
    if (group.id === id) {
      group['users'].forEach((user: any) => {
        usersToRemoveIds.push(user.id)
      })
    }
  })
  await fetch(`api/v1/groups/${id}/delete-members`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(usersToRemoveIds)
  });

  const response = await fetch(`api/v1/groups/${id}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  if (response.ok === true) {
    document.getElementById(`group-${id}`).style.display = "none"
    document.getElementById("modal-delete-group-open").style.display = "none"
    window.location.reload()
  }
}

const clearEventListeners = (id: number, groups: any[]) => {
  document.getElementById("group-delete-confirm").removeEventListener("click", () => deleteGroup(id, groups))
  document.getElementById("group-delete-x").removeEventListener("click", () => clearEventListeners(id, groups))
  document.getElementById("group-delete-cancel").removeEventListener("click", () => clearEventListeners(id, groups))
  document.getElementById("modal-delete-group-open").style.display = "none"
}

const wireModal = (id: number, groups: any[]) => {
  document.getElementById("modal-delete-group-open").style.display = "block"
  groups.forEach((group) => {
    if (group.id === id) {
      document.getElementById("group-delete-modal-body").innerText = `Are you sure you want to delete this group? All ${group['users'].length} users will be removed from the group`
    }
  })
  document.getElementById("group-delete-confirm").addEventListener("click", () => deleteGroup(id, groups))
  document.getElementById("group-delete-x").addEventListener("click", () => clearEventListeners(id, groups))
  document.getElementById("group-delete-cancel").addEventListener("click", () => clearEventListeners(id, groups))
}

const toggleGroupView = (id: number) => {
  document.getElementById(`groups-user-list-${id}`).style.display = document.getElementById(`groups-user-list-${id}`).style.display === "none" ? "block" : "none"
  document.getElementById(`groups-repository-${id}`).style.display = document.getElementById(`groups-repository-${id}`).style.display === "none" ? "block" : "none"
  document.getElementById(`group-toggle-button-${id}`).innerText = document.getElementById(`group-toggle-button-${id}`).innerText === "visibility" ? "visibility_off" : "visibility"

}

const getSubscriptions = async () => {
  const userId = localStorage.getItem("userId")
  const subscriptionResponse = await fetch(`api/v1/subscribe/${userId}`)
  return subscriptionResponse.json()

}

export function ShowAllGroups({setViewGroupId}: any) {

  const [groups, setGroups] = React.useState([]);
  const [subscriptions, setSubscriptions] = React.useState([]);
  const [editGroup, setEditGroup] = React.useState({
    "id": -1,
    "shortName": "",
    "longName": "",
    "alias": "",
    "repositoryId": -1,
    "token": "",
    "canEdit": false
  });
  const userId = localStorage.getItem("userId")

  useEffect(() => {
    getAllGroups().then((result) => {
      setGroups(result)
    })
    getSubscriptions().then((result) => {
      setSubscriptions(result)
    })
  }, [])

  const isTeacher = localStorage.getItem("isTeacher") === "true"

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

  const subscribeUserToGroup = async (groupId: number) => {
    await fetch(`api/v1/subscribe`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "userId": userId,
        "groupId": groupId
      })
    });
    getSubscriptions().then((result) => {
      setSubscriptions(result)
    })
  }

  const unsubscribeUserToGroup = async (groupId: number) => {
    await fetch(`api/v1/unsubscribe`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "userId": userId,
        "groupId": groupId
      })
    });
    getSubscriptions().then((result) => {
      setSubscriptions(result);
    })
  }

    function groups_page_user_list(group: any) {
        if(group['users'].length === 0){
            return (
                <div style={{paddingTop: '1em', paddingBottom: '1em'}} id={`groups-no-member-message-${group['id']}`}>
                    This group has no members
                </div>
            )
        }
        return (
            <div className={"groups-page-user-list"} id={`groups-user-list-${group['id']}`}>
                <div className={"table"} id={"group-list"}>
                    <div className={"groups-header"}>
                        <div className="tableCell"><b>Name</b></div>
                        <div className="tableCell"><b>Username</b></div>
                        <div className="tableCell"><b>Alias</b></div>
                        <div className="tableCell"><b>Roles</b></div>
                    </div>
                    {group['users'].map((user: any) => (
                        <div className="tableRow" id={`group-${group['id']}-user-${user.id}`} key={user.id}>
                            <div className="tableCell">{user['firstName']} {user['lastName']}</div>
                            <div className="tableCell">{user['username']}</div>
                            <div className="tableCell">{user['nickName']}</div>
                            <div className="tableCell">{formatRoles(user['roles'])}</div>
                        </div>
                    ))}
                </div>
            </div>
        )
    }

    const isMember = (group: any) => {
    console.log(group)
    console.log(group.users.map((user: any) => user.id))
      return group.users.map((user: any) => user.id).includes(parseInt(userId))
    }


  return (
      <div>
        {groups.map((group: any) => (
            <div className={'raised-card group'} id={`group-${group['id']}`} key={group['id']}>
              <div className={"group-header"}>
                {group['shortName'] !== 'Teachers' && group['shortName'] !== 'Non Group' && isTeacher ?
                    <div className={"delete-group"}>
                      <span className={"material-icons"}
                            onClick={() => wireModal(group['id'], groups)}>clear</span>
                    </div>
                    : ""}
                {isTeacher ?
                    <button className="button edit-group-button" id="edit-group"
                            data-privilege="teacher" onClick={() => {
                      document.getElementById("modal-edit-group-members-open").style.display = "block";
                      setViewGroupId(group.id)
                    }}> Manage Group Members</button>
                    : ""}
                <div>
                  {!isMember(group) ? (subscriptions.includes(group.id) ? <button className={"button subscribe-button"}
                                                              onClick={() => unsubscribeUserToGroup(group.id)}>Unsubscribe</button> :
                      <button className={"button subscribe-button"}
                              onClick={() => subscribeUserToGroup(group.id)}>Subscribe</button>): ""}
                </div>
                <div>
                  <button className={"button show-group-feed-button"}
                          onClick={() => window.location.href = `group_feed/${group['id']}`}>View
                    Feed
                  </button>
                </div>
                <div id={`groupSettingsIcon${group.id}`}>
                            <span className={"material-icons group-settings"}
                                  id={`group${group.id}`} onClick={() => {
                              setEditGroup(group);
                              document.getElementById("group-settings-modal-open").style.display = 'block';
                            }}>settings</span>
                </div>
                <div id={`toggle-group-details-${group['id']}`}>
                  <span className={"material-icons toggle-group-details"}
                        id={`group-toggle-button-${group['id']}`}
                        onClick={() => toggleGroupView(group['id'])}>visibility</span>
                </div>
                <h2 className={'group-name-short'}>{group['shortName']}</h2>
              </div>
              <h3 className={'group-name-long'}>{group['longName']}</h3>
              {groups_page_user_list(group)}
              <div className={"groups-page-repository"} id={`groups-repository-${group['id']}`}>
                <h2>Repository Information:</h2>
                {group.token.length > 0 ?
                    group.token === "INVALID" ?
                      <p style={{color:"red"}}>The token provided is either expired or invalid</p>
                      :
                      <>
                        <h2 className={'group-repository-title'}>{group.alias}</h2>
                        <div className={"branch-box"}>
                          <h3 className={'group-repository-title'} style={{margin: 0}}>Branches</h3>
                          <div className={"table"} id={"group-list-branches"}>
                            {group.branches.map((branch: any) => (
                                <div className="tableRow" style={{display: "grid", minHeight: "40px"}}>
                                  <div className="tableCell">
                                    <a href={branch.web_url}
                                       target="_blank">{branch.name} ({group.commits.length} commits)</a>
                                    <br></br>
                                  </div>
                                </div>
                            ))}
                          </div>
                        </div>
                        <div className={"commit-box"}>
                          <h3 className={'group-repository-title'}
                              style={{marginBottom: 0}}>Commits</h3>
                          <div className={"table"} id={"group-list-commits"}>
                            {group.commits.map((commit: any) => (
                                <div className="tableRow">
                                  <div className="tableCell">
                                    <strong>Name:</strong>{commit['author_name']} <br></br>
                                    <strong>Message:</strong> {commit['message']} <br></br>
                                    <strong>ID:</strong><a href={commit['web_url']}
                                                           target="_blank">{commit['id']}</a> <br></br>
                                  </div>
                                </div>
                            ))}
                          </div>
                        </div>
                      </>
                    :
                    <p style={{color:"orange"}}>This group does not have any repository associated with it.</p>
                }
              </div>
            </div>
        ))}
        <GroupSettingsModal editGroup={editGroup}/>
      </div>

  );
}