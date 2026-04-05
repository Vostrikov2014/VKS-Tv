import { Box, Typography, Card, CardContent } from '@mui/material';

export default function UsersPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        Users
      </Typography>
      <Card>
        <CardContent>
          <Typography variant="body1">
            User management interface - Coming soon
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={2}>
            This page will display user management with role assignment,
            tenant administration, and access control features.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
