import * as React from "react";

export function RemoveMemberModal() {
    return (
        <div className="modal-container" id="modal-delete-open">
            <div className="modal-delete">
                <div className="modal-header">
                    <div className="modal-title">
                        Delete Confirmation
                    </div>

                    <div className="modal-close-button" id="modal-delete-x">&times;</div>
                </div>
                <div className="border-line"/>
                <div className="modal-body">
                    Are you sure you want to remove this user from the group?
                </div>
                <div className="modal-buttons">
                    <button className="button dangerous-button" id="modal-delete-confirm">Remove</button>
                    <button className="button" id="modal-delete-cancel">Cancel</button>
                </div>
            </div>
        </div>
    );
}