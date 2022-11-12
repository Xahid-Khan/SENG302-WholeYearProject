import * as React from "react";
import {FormEvent} from "react";

export function EditGroupSettings({group}: any) {
  if (group == undefined) {
    return (<></>);
  }

  const [longName, setLongName] = React.useState(group.longName);
  const [repositoryID, setRepositoryID] = React.useState(group.repositoryId == -1? "" : group.repositoryId);
  const [repositoryToken, setRepositoryToken] = React.useState(group.token || "");
  const [alias, setAlias] = React.useState(group.alias || "");
  const [longCharCount, setLongCharCount] = React.useState(group.longName.length);

  const handleCancel = () => {
    document.getElementById("group-settings-modal-open").style.display = "none"
  }

  const validateRepositoryInfo = async (e: FormEvent) => {
    if (longName.length === 0) {
      document.getElementById("edit-group-error").innerText = "Please provide a long name for the group";
      return;
    }

    if (!(alias.length == 0 && repositoryID.length == 0 && repositoryToken.length == 0)) {
      if (alias.length === 0) {
        document.getElementById("edit-group-error").innerText = "Please provide an Alias name for the repository";
        return;
      }

      if (isNaN(parseInt(repositoryID)) && repositoryID <= 0) {
        document.getElementById("edit-group-error").innerText = "Repository ID must be a number."
        return;
      }

      if (!repositoryToken.match("[0-9a-zA-Z-]{20}")) {
        document.getElementById("edit-group-error").innerText = "Please enter a valid Token"
        return;
      }
    }

    await fetch(`groups/update_repository`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "longName": longName,
        "alias": alias,
        "groupId": group.id,
        "repositoryId": repositoryID,
        "token": repositoryToken
      })
    }).then((res) => {
      if (res.ok === true) {
        window.location.reload()
      } else {
        document.getElementById("edit-group-error").innerText = "Something went wrong, Please check if you've permission to edit this repository"
      }
    }).catch((e) => {
      console.log("error ", e)
    })
  }

  return (
      <div>
          <div className={"edit-group-settings-container"}>
            <div className={"edit-group-settings"} style={{width: "100%"}}>
              <div className={"edit-group-form raised-card"}>
                <h3>Group Settings</h3>
                <div>
                  <label className={"settings-title"}>Short Name:</label>
                  <label>{group.shortName}</label>
                </div>
                <br/>
                <div className={"settings-long-name"}>
                  <label className={"settings-title"}>Long Name:</label>
                  {group.canEdit ?
                      <input type="text" name="long-name" className="input-name" id={"long-name"}
                             placeholder={group.longName} maxLength={64} onChange={(e) => {
                        setLongName(e.target.value);
                        setLongCharCount(e.target.value.length)
                      }}/>
                      : <label>  {group.longName}</label>}
                  {group.canEdit ? <span className="input-length"
                                             id="long-name-length">{longCharCount} / 64</span> : ""}
                </div>
                <h3>Repo Settings</h3>
                <form  onSubmit={(e) => {e.preventDefault(); validateRepositoryInfo(e);}}>
                  <table>
                    <tr>
                      <td>
                        <label className={"settings-title"}>Alias:</label>
                      </td>
                      <td>
                        {group.canEdit ?
                            <input type="text" name="alias" className="input-name"
                                   id={"alias"} defaultValue={group.alias}
                                   maxLength={64} onChange={(e) => {
                              setAlias(e.target.value)
                            }}/>
                            : <label>Default alias</label>}
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label>Repository ID:</label>
                      </td>
                      <td>
                        {group.canEdit ?
                            <input type="text" name="repository-id" className="input-name"
                                   defaultValue={group.repositoryId != -1 ? group.repositoryId : ""}
                                   id={"repository-id"} maxLength={64} onChange={(e) => {
                              setRepositoryID(e.target.value)
                            }}/>
                            : <label>Default alias</label>}
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label>Token:</label>
                      </td>
                      <td>
                        {group.canEdit ?
                            <input type="text" name="repository-token" className="input-name"
                                   defaultValue={group.token}
                                   id={"repository-token"} maxLength={64} onChange={(e) => {
                              setRepositoryToken(e.target.value)
                            }}/>
                            : <label>Default alias</label>}
                      </td>
                    </tr>
                  </table>
                  <div className="form-error" id="edit-group-error"/>
                  {group.canEdit ?
                      <div className="modal-buttons">
                        <button className="button" id="group-repository-save" type={"submit"}>Save
                        </button>
                        <button onClick={() => handleCancel()} className="button" type={"reset"}
                                id="group-settings-cancel">Cancel
                        </button>
                      </div>
                      : ""}
                </form>
              </div>
            </div>
          </div>
        </div>
  );
}