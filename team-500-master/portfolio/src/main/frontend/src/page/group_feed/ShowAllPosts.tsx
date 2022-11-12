import React, {FormEvent, useEffect, useRef} from "react";
import {PostAndCommentContainer} from "./PostAndCommentContainer";
import {EditPostDataModal} from "./EditPostDataModal";
import {Tooltip} from "@mui/material";

export function ShowAllPosts() {

  const urlData = document.URL.split("?")[0].split("/");
  const viewGroupId = urlData[urlData.length - 1];
  const userId = parseInt(localStorage.getItem("userId"));
  const [newComment, setNewComment] = React.useState("");
  const username = localStorage.getItem("username");
  const [title, setTitle] = React.useState("");
  const [content, setContent] = React.useState('');
  const [editPostId, setEditPostId] = React.useState(-1);
  const [longCharacterCount, setLongCharacterCount] = React.useState(0);
  const isTeacher = localStorage.getItem("isTeacher") === "true";
  const loadRef = useRef(null)
  const [wasLastList, setWasLastList] = React.useState(false);
  const [updateState, setUpdateState] = React.useState(true);
  const [groupPosts, setGroupPosts] = React.useState({
        "groupId": -1,
        "shortName": "",
        "isSubscribed": false,
        "isMember": false,
        "posts": [{
          "postId": -1,
          "reactions": [],
          "comments": []
        }]
      }
  )

  const [offset, setOffset] = React.useState(0);
  const loadOptions: any = {
    root: null,
    rootMargin: "0px",
    threshold: 1.0
  }
  const getPosts = async () => {
    const currentGroupResponse = await fetch(`feed_content/${viewGroupId}?offset=` + offset);
    return currentGroupResponse.json()
  }

  const getPostById = async (editPostId: any) => {
    const currentPostResponse = await fetch(`get_post/${editPostId}`);
    return currentPostResponse.json()
  }

  // With regards to https://dev.to/producthackers/intersection-observer-using-react-49ko
  useEffect(() => {
    const observer = new IntersectionObserver((entries) => {
      const [ entry ] = entries
      if(entry.isIntersecting) {
        if (!wasLastList) {
          getPosts().then((result) => {
            if (result.posts.length == 0) {
              setWasLastList(true);
              return;
            }
            if (groupPosts.groupId != -1 && groupPosts.posts.length > 0) {
              const totalPosts = groupPosts.posts.concat(result.posts);
              setGroupPosts({
                "groupId": result.groupId,
                "shortName": result.shortName,
                "isSubscribed": result.isSubscribed,
                "isMember": result.isMember,
                "posts": totalPosts
              });
            } else {
              setGroupPosts(result);
            }
            setOffset(offset + 1);
          })
        }
      }
    }, loadOptions)
    if (loadRef.current) observer.observe(loadRef.current)

    return () => {
      if (loadRef.current) observer.unobserve(loadRef.current)
    }

  }, [loadRef, loadOptions, updateState])


  useEffect(() => {
    if (!isNaN(Number(viewGroupId))) {
      getCurrentGroup().then((result: any) => {
        setGroupPosts(result)
        setOffset(offset + 1);
      }).catch((error) => {
        console.log(error);
      })
    }
  }, [])

  const getCurrentGroup = async () => {
    const currentGroupResponse = await fetch(`feed_content/${viewGroupId}`, {
      method: "GET",
      "headers": {
        'Content-Type': 'application/json'
      }
    })
    return currentGroupResponse.json()
  }

  const handleCancelEditPost = () => {
    setContent("");
    setTitle("");
    setEditPostId(-1);
    document.getElementById("edit-post-modal-open").style.display = "none";
  }

  const validateCreateForm = async (formEvent: FormEvent) => {
    formEvent.preventDefault()
    let errors = false
    let errorMessage

    if (errors) {
      errorMessage = "Please fill all the fields"
      document.getElementById("create-post-error").innerText = errorMessage;
    } else {
      await fetch(`update_feed/${editPostId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({"groupId": viewGroupId, "postContent": content})
      }).then((res) => {
        if (res.ok === true) {
          window.location.reload();
        } else {
          document.getElementById("create-post-error").innerText = "You do not have permission to post in this group."
        }
      }).catch((e) => {
        console.log("error ", e)
      })
    }
  }

  const openConfirmationModal = (postId: any) => {
    document.getElementById(`modal-delete-open`).style.display = 'block';
    document.getElementById(`modal-delete-x`).addEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-cancel`).addEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-confirm`).addEventListener("click", () => confirmDeleteModal(postId));
  }

  const cancelDeleteModal = (postId: any) => {
    document.getElementById(`modal-delete-open`).style.display = 'none';
    document.getElementById(`modal-delete-x`).removeEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-cancel`).removeEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-confirm`).removeEventListener("click", () => confirmDeleteModal(postId));
  }

  const confirmDeleteModal = async (postId: any) => {
    document.getElementById(`modal-delete-x`).removeEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-cancel`).removeEventListener("click", () => cancelDeleteModal(postId));
    document.getElementById(`modal-delete-confirm`).removeEventListener("click", () => confirmDeleteModal(postId));

    await fetch(`delete_feed/${postId}`, {
      method: 'DELETE'
    })
    window.location.reload();
  }

  const clickHighFive = async (id: number) => {
    const button = document.getElementById(`high-five-${id}`)
    if(button.style.backgroundSize === "100% 100%") {
      button.style.backgroundSize = "0% 100%";
    } else {
      button.style.backgroundSize = "100% 100%";
    }
    button.style.backgroundSize = button.style.backgroundSize === "100% 100%" ? "0 100%" : "100% 100%"

    await fetch('post_high_five', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "postId": id
      })
    });
    let posts = groupPosts
    await getPostById(id).then((result: any) => {
      for (let i = 0; i < posts.posts.length; i++) {
        if (posts.posts[i].postId == result.postId) {
          posts.posts[i] = result;
          break;
        }
      }
    })
    setGroupPosts(posts)
    setUpdateState(!updateState);
  }

  const toggleCommentDisplay = (id: number) => {
    const commentsContainer = document.getElementById(`comments-container-${id}`)
    commentsContainer.style.display = commentsContainer.style.display === "block" ? "none" : "block"
  }

  const makeComment = async (id: number) => {
    setNewComment(document.getElementById(`comment-content-${id}`).getAttribute('value'));

    if (newComment.length != 0 && newComment.length < 4096) {
      await fetch(`add_comment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          "userId": localStorage.getItem("userId"),
          "postId": id,
          "comment": newComment
        })
      });

      setNewComment("");

      let posts = groupPosts
      await getPostById(id).then((result: any) => {
        for (let i = 0; i < posts.posts.length; i++) {
          if (posts.posts[i].postId == result.postId) {
            posts.posts[i] = result;
            break;
          }
        }
      })
      setGroupPosts(posts)
      setUpdateState(!updateState);
      document.getElementById(`post-comments-${id}`).scrollTop = document.getElementById(`post-comments-${id}`).scrollHeight;
    }
  }

  const subscribeUserToGroup = async (groupId: number) => {
    await fetch(`../api/v1/subscribe`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "userId": userId,
        "groupId": groupId
      })
    });
    setUpdateState(!updateState);
    getCurrentGroup().then((response) => {
      setGroupPosts(response);
    })
  }

  const unsubscribeUserToGroup = async (groupId: number) => {
    await fetch(`../api/v1/unsubscribe`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        "userId": userId,
        "groupId": groupId
      })
    });
    setUpdateState(!updateState);
    getCurrentGroup().then((response) => {
      setGroupPosts(response);
    })
  }


  return (
      <div>
        {
          groupPosts.groupId !== -1 ?
              <>
                <div className={"group-feed-name"}>{groupPosts.shortName} Feed</div>
                {!groupPosts.isMember ?
                    <>
                      {
                        groupPosts.isSubscribed ?
                            <button className={"feed-Sub-Button"}
                                    onClick={() => unsubscribeUserToGroup(groupPosts.groupId)}>Unsubscribe</button>
                            :
                            <button className={"feed-Sub-Button"}
                                    onClick={() => subscribeUserToGroup(groupPosts.groupId)}>Subscribe</button>
                      }
                    </>
                    :
                    <>
                      <Tooltip title={"You cannot unsubscribe if you're a member of the group."}>
                        <span className={"feed-Sub-Button"}
                              style={{padding: "5px", marginTop: "-35px"}}>
                          <button disabled={true}>Unsubscribe</button>
                        </span>
                      </Tooltip>
                    </>
                }
                {
                  groupPosts.posts.length > 0 ?
                      <>
                        {groupPosts.posts.map((post: any) => (
                            <>
                              <PostAndCommentContainer post={post} isTeacher={isTeacher}
                                                       setContent={setContent}
                                                       setLongCharacterCount={setLongCharacterCount}
                                                       setTitle={setTitle}
                                                       setEditPostId={setEditPostId}
                                                       clickHighFive={clickHighFive}
                                                       openConfirmationModal={openConfirmationModal}
                                                       toggleCommentDisplay={toggleCommentDisplay}
                                                       makeComment={makeComment}
                                                       setNewComment={setNewComment}
                                                       username={username}
                                                       updateState={updateState}
                              />
                            </>)
                        )}
                        <div ref={loadRef}/>
                      </>
                      :
                      <div className={"raised-card group-post"} key={"-1"}>
                        <h3>There are no posts</h3>
                      </div>
                }
                <EditPostDataModal handleCancelEditPost={handleCancelEditPost}
                                   longCharacterCount={longCharacterCount}
                                   validateCreateForm={validateCreateForm}
                                   title={title}
                                   content={content}
                                   setContent={setContent}
                                   setLongCharacterCount={setLongCharacterCount}

                />

              </>
              :
              <div>
                <h1>Loading...</h1>
              </div>
        }
      </div>

  )
}
