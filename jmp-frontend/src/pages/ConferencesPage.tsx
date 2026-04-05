import { Box, Typography, Card, CardContent } from '@mui/material';

export default function ConferencesPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        Conferences
      </Typography>
      <Card>
        <CardContent>
          <Typography variant="body1">
            Conference management interface - Coming soon
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={2}>
            This page will display a list of all conferences with filtering, 
            pagination, and actions to create, edit, and delete conference rooms.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
