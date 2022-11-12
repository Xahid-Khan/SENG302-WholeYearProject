import React from "react";
import {ToasterRoot} from "../../component/toast/ToasterRoot";
import {ShowAllPosts} from "./ShowAllPosts";
import {EditGroupMembersModal} from "../group_page/EditGroupMembersModal";
import {CreatePostModal} from "./CreatePostModal";
import {Socket} from "../../entry/live_updating";

export function GroupFeedPage() {

    const [viewGroupId, setViewGroupId] = React.useState(-1)
    const isMember = localStorage.getItem("isMember") === "true"

    return (
        <ToasterRoot>
            <div className="create-post">
                <div>
                    {!isMember ? "" :
                        <button className="button add-group-button" id="add-group"
                                onClick={() => document.getElementById("create-post-modal-open").style.display='block'}> Create
                            Post
                        </button>

                    }
                </div>
            </div>
            <div className={"raised-card groups-feed"}>
                <ShowAllPosts/>
            </div>
            <CreatePostModal viewGroupId={viewGroupId}/>
        </ToasterRoot>

    )
}