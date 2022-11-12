import {DatetimeUtils} from "../../util/DatetimeUtils";
import React from "react";

export function PostAndCommentContainer (props: any) {

  return(
      <div className={"raised-card group-post"} key={props.post.postId}>
        <div className={"post-header"} key={"postHeader" + props.post.postId}>
          <div className={"post-info"} key={"postInfo" + props.post.postId}>
            <div>{props.post.username}</div>
            <div
                className={"post-time"}>{DatetimeUtils.timeStringToTimeSince(props.post.time)}</div>
          </div>
          {props.post.userId == parseInt(localStorage.getItem("userId")) || props.isTeacher ?
              <>
                <div className={"post-edit"}>
                            <span className={"material-icons"}
                                  onClick={() => {
                                    props.setContent(props.post.content);
                                    props.setLongCharacterCount(props.post.content.length);
                                    props.setTitle(props.post.username);
                                    props.setEditPostId(props.post.postId);
                                    document.getElementById("edit-post-modal-open").style.display = 'block';
                                  }}
                                  id={`post-edit-${props.post.postId}`}>edit</span>
                </div>
                <div className={"post-delete"}>
                            <span className={"material-icons"}
                                  onClick={() => {
                                    props.openConfirmationModal(props.post.postId);
                                  }}
                                  id={`post-delete-${props.post.postId}`}>clear</span>
                </div>
              </>
              :
              ""}
        </div>
        <div className={"post-body"}
             key={"postBody" + props.post.postId}>{props.post.content}</div>
        <div className={"border-line"}/>
        <div className={"post-footer"}>
          <div className={"high-five-container"}>
            <div className={"high-fives"}>
              <div className={"high-five-overlay"}>
                <span className={"high-five-text"}>High Five!</span> <span
                  className={"material-icons"}>sign_language</span>
              </div>
              <div className={"high-five"} id={`high-five-${props.post.postId}`}
                   style={{backgroundSize: props.post.reactions.includes(props.username) ? "100% 100%" : "0% 100%"}}
                   onClick={() => props.clickHighFive(props.post.postId)}>
                <span className={"high-five-text"}>High Five!</span> <span
                  className={"material-icons"}>sign_language</span>
              </div>
              <div className={"high-five-list"}>
                <div className={"high-five-count"}><span
                    className={"material-icons"}
                    style={{fontSize: 15}}>sign_language</span>{props.post.reactions.length}
                </div>
                <div className={"border-line high-five-separator"}/>
                {props.post.reactions.map((highFiveName: string) => (
                    <div className={"high-five-names"}
                         key={highFiveName}>{highFiveName}</div>
                ))}
              </div>
            </div>
          </div>
          <div className={"comments-icon-container"}>
            <div className={"comments-select"}
                 onClick={() => props.toggleCommentDisplay(props.post.postId)}>
              <span className={"comments-select-text"}>Comments</span> <span
                className={"material-icons"}>mode_comment</span>
            </div>
          </div>
        </div>
        <div className={"comments-container"}
             id={`comments-container-${props.post.postId}`}>
          <div className={"border-line"}/>
          <div className={"post-comments"} id={`post-comments-${props.post.postId}`}>
            {props.post.comments.map((comment: any) => (
                    <div className={"post-comment-container"} key={comment.commentId}>
                      <div
                          className={"comment-name"}>{comment.username} ({DatetimeUtils.timeStringToTimeSince(comment.time)})
                      </div>
                      <div className={"post-comment"}>{comment.content}</div>

                    </div>
                )
            )}
          </div>
          <form className={"make-comment-container"} onSubmit={(e) => {
            e.preventDefault();
            props.makeComment(props.post.postId);
            e.currentTarget.reset();
          }}>
            <div className={"input-comment"}>
              <input type={"text"} className={"input-comment-text"}
                     id={`comment-content-${props.post.postId}`}
                     minLength={1}
                     maxLength={4095}
                     onChange={(e) => props.setNewComment(e.target.value.trim())}
                     placeholder={"Comment on post..."}/>
            </div>
            <div className={"submit-comment"}>
              <button className={"button submit-comment-button"} type={"submit"}
                      id={`comment-submit-${props.post.postId}`}>
                Add comment
              </button>
            </div>
          </form>
        </div>
      </div>
  )
}