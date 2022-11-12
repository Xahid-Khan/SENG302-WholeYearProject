import * as React from 'react';

import {ToasterRoot} from "../../component/toast/ToasterRoot";
import {ShowAllGroups} from "./ShowAllGroups";
import {CreateGroupModal} from "./CreateGroup";
import {DeleteGroupModal} from "./DeleteGroupModal";
import {EditGroupMembersModal} from "./EditGroupMembersModal";
import {RemoveMemberModal} from "./RemoveMemberModal";
import {GroupSettingsModal} from "./GroupSettingsModal";

/**
 * The root of the GroupPage. This does a few jobs:
 * 1. Construct the GroupPage and wrap the whole page in a Provider, so it can be used by children.
 * 2. Wrap the entire page in a PageLayout.
 * 3. Place the PageContent component inside that Layout component.
 */
export const GroupPage = () => {

  const [viewGroupId, setViewGroupId] = React.useState(-1)

  const isTeacher = localStorage.getItem("isTeacher") === "true"

  return (
      <ToasterRoot>
        <div className="add-group">
          <div>
            {isTeacher ? <button className="button add-group-button" id="add-group"
                                 onClick={() => document.getElementById("modal-create-group-open").style.display = "block"}> Create
              Group
            </button> : ""
            }
          </div>
        </div>
        <div className={"raised-card groups"}>
          <h1>Groups</h1>
          <ShowAllGroups setViewGroupId={setViewGroupId}/>
        </div>
        <CreateGroupModal/>
        <DeleteGroupModal/>
        <EditGroupMembersModal viewGroupId={viewGroupId}/>
        <RemoveMemberModal/>
      </ToasterRoot>
  )
}