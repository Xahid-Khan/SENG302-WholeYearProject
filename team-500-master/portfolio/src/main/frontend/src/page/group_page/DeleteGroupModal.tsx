import * as React from "react";

export function DeleteGroupModal() {
    return (
        <div className={"modal-container"} id={"modal-delete-group-open"}>
            <div className={"modal-create-group"}>
                <div className={"modal-header"}>
                    <div className={"modal-title"}>
                        Delete group
                    </div>
                    <div className={"modal-close-button"} id={"group-delete-x"}>&times;</div>
                </div>
                <div className={"border-line"}/>
                <div className="modal-body" id={"group-delete-modal-body"}>
                    Are you sure you want to delete this group? All users will be removed from the group
                </div>
                <div className="modal-buttons">
                    <button className="button dangerous-button" id="group-delete-confirm">Delete</button>
                    <button className="button" id="group-delete-cancel">Cancel</button>
                </div>
            </div>
        </div>
    );
}