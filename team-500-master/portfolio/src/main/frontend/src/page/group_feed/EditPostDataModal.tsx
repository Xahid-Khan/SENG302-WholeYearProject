import React from "react";

export function EditPostDataModal(props: any) {
  return (<div className={"modal-container"} id={"edit-post-modal-open"}>
    <div className={"modal-edit-post"}>
      <div className={"modal-header"}>
        <div className={"modal-title"}>
          Edit Post
        </div>
        <div className={"modal-close-button"} id={"edit-post-cancel-x"}
             onClick={props.handleCancelEditPost}>&times;</div>
      </div>
      <div className={"border-line"}/>

      <form onSubmit={(e) => {
        if (props.longCharacterCount > 0) props.validateCreateForm(e)
      }}>
        <div className="modal-body modal-edit-post-body">
          <label className={"post-title"}>{props.title}</label>
          <br/>
          <br/>
          <span id={"edit-modal-error"}></span>
        </div>

        <div className={"post-description"}>
          <label className={"settings-description"}>Content:</label>
          <br/>
          <textarea className={"text-area"} id={`edit-post-content`} required
                    defaultValue={props.content}
                    cols={50} rows={10} maxLength={4096} onChange={(e) => {
            props.setContent(e.target.value.trim());
            props.setLongCharacterCount(e.target.value.trim().length);
            if (props.longCharacterCount > 0) {
              document.getElementById("edit-post-save").removeAttribute("disabled");
            } else {
              document.getElementById("edit-post-save").setAttribute("disabled", "true");
            }
          }}/>
          <span className="content-length" id="content-length">{props.longCharacterCount} / 4096</span>
          <br/>
        </div>
        <div className="form-error" id="create-post-error"/>


        <div className="modal-buttons">
          <button className="button" id="edit-post-save" type="submit">Save</button>
          <button className="button" type="reset" id="create-post-cancel"
                  onClick={props.handleCancelEditPost}>Cancel
          </button>
        </div>
      </form>
    </div>
  </div>)
}