import React, {useEffect} from "react";
import {observer} from "mobx-react-lite";
import {AppBar, Box, Button, Toolbar, Typography} from "@mui/material";
import {NotificationDropdown} from "../notifications/NotificationDropdown";
import {AccountDropdown} from "./AccountDropdown";
import {getAbsolutePath} from "../../util/RelativePathUtil";
import {MessageButton} from "./MessageButton";

export const navigateTo = (page: string) => {
  const globalUrlPathPrefix = window.localStorage.getItem("globalUrlPathPrefix")
  window.location.href = getAbsolutePath(globalUrlPathPrefix, page)
}

export const NavBar: React.FC = observer(() => {

  const globalUrlPathPrefix = window.localStorage.getItem("globalUrlPathPrefix")

  useEffect(() => {
    setInterval(async function sync() {
      const res = await fetch(`${globalUrlPathPrefix}/api/v1/validate_token`)
      if (!res.ok) {
        alert("Your roles have changed. Please login again.")
        window.location.replace("logout");
      }
    }, 5000)
  }, []);

  return (
      <React.Fragment>
        <AppBar position="fixed" sx={{bgcolor: "#788"}}>
          <Toolbar>
            <Box style={{display: "flex", paddingTop: 8}}>
              <img src={`${globalUrlPathPrefix}/images/SprintrLogoTransparent.png`}
                   style={{maxWidth: 30}} alt={"Sprintr Logo"}/>
              <Typography
                  variant="h5">ENG SPRINTR</Typography>
            </Box>

            <Box sx={{pl: 2, flexGrow: 1}}>
              <Button color='inherit' onClick={() => navigateTo("home_feed")}>
                <Typography textAlign="center">Home</Typography>
              </Button>
              <Button color='inherit' onClick={() => navigateTo("project-details")}>
                <Typography textAlign="center">Project</Typography>
              </Button>
              <Button color='inherit' onClick={() => navigateTo("user-list")}>
                <Typography textAlign="center">Users</Typography>
              </Button>
              <Button color='inherit' onClick={() => navigateTo("groups")}>
                <Typography textAlign="center">Groups</Typography>
              </Button>
            </Box>

            <MessageButton></MessageButton>
            <NotificationDropdown></NotificationDropdown>
            <AccountDropdown></AccountDropdown>

          </Toolbar>
        </AppBar>
        <Toolbar sx={{mb: 3}}/>
      </React.Fragment>
  )
})