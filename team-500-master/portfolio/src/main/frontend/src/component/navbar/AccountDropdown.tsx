import React from "react";
import {observer} from "mobx-react-lite";
import {Avatar, Box, IconButton, Menu, MenuItem, Typography} from "@mui/material";
import {navigateTo} from "./NavBar";

export const AccountDropdown: React.FC = observer(() => {
    const globalImagePath = localStorage.getItem("globalImagePath");
    const username = localStorage.getItem("username");
  const userId = parseInt(window.localStorage.getItem("userId"))

  //the element that was last clicked on
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  //uses the last clicked element to determine which menu to open
  const open2 = anchorEl?.id === 'account-button';

  return (
      <React.Fragment>
        <Box sx={{flexGrow: 0, display: 'flex', alignItems: 'center'}}>
            <Typography textAlign="left">{username}</Typography>
              <IconButton
                  id='account-button'
                  onClick={handleClick}
                  size="medium"
                  aria-controls={open ? 'account-menu' : undefined}
                  aria-haspopup="true"
                  aria-expanded={open ? 'true' : undefined}>
                <Avatar src={`//${globalImagePath}${userId}`}/>
              </IconButton>
        </Box>
        <Menu
            anchorEl={anchorEl}
            id="account-menu"
            open={open2}
            onClose={handleClose}
            onClick={handleClose}
            PaperProps={{sx: {maxHeight: 0.5}}}
            transformOrigin={{horizontal: 'right', vertical: 'top'}}
            anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
        >
          <MenuItem onClick={() => navigateTo("my_account")}>
            Account
          </MenuItem>
          <MenuItem onClick={() => {
            navigateTo("logout");
            window.localStorage.clear();
          }}>
            Log out
          </MenuItem>
        </Menu>
      </React.Fragment>
  )
})