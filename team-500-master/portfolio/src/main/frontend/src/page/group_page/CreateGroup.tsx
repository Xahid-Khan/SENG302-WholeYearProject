import * as React from "react";
import {CreateGroupForm} from "./CreateGroupForm";

export function CreateGroupModal() {
    return (
        <div className={"modal-container"} id={"modal-create-group-open"}>
            <div className={"modal-create-group"}>
                <div className={"modal-header"}>
                    <div className={"modal-title"}>
                        Create group
                    </div>
                    <div className={"modal-close-button"} onClick={() => document.getElementById("modal-create-group-open").style.display = "none"}>&times;</div>
                </div>
                <div className={"border-line"}/>
                <div className={'modal-body'}>
                    <CreateGroupForm/>
                </div>
            </div>
        </div>
    );
}